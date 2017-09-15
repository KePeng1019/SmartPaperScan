package com.pengke.paper.scanner.scan

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.media.MediaActionSound
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import com.pengke.paper.scanner.SourceManager
import com.pengke.paper.scanner.crop.CropActivity
import com.pengke.paper.scanner.processor.processPicture
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import rx.Observable
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScanPresenter constructor(context: Context, iView: IScanView.Proxy)
    : SurfaceHolder.Callback, Camera.PictureCallback, Camera.PreviewCallback {
    private val TAG: String = "ScanPresenter"
    private val iView: IScanView.Proxy
    private val context: Context
    private var mCamera: Camera? = null
    private val mSurfaceHolder: SurfaceHolder
    private val executor: ExecutorService
    private val proxySchedule: Scheduler
    private var busy: Boolean = false

    init {
        this.iView = iView
        this.context = context
        mSurfaceHolder = iView.getSurfaceView().holder
        mSurfaceHolder.addCallback(this)
        executor = Executors.newSingleThreadExecutor()
        proxySchedule = Schedulers.from(executor)
    }

    fun start() {
        mCamera?.startPreview() ?: Log.i(TAG, "camera null")
    }

    fun stop() {
        mCamera?.stopPreview() ?: Log.i(TAG, "camera null")
    }

    fun shut() {
        busy = true
        Log.i(TAG, "try to focus")
        mCamera?.autoFocus { b, _ ->
            Log.i(TAG, "focus result: " + b)
            mCamera?.takePicture(null, null, this)
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        } catch (e: RuntimeException) {
            e.stackTrace
            Toast.makeText(context, "open camera error", Toast.LENGTH_SHORT).show()
            iView.exit()
        }


        val param = mCamera?.parameters
        val size = getMaxResolution()
        param?.setPreviewSize(size?.width ?: 1920, size?.height ?: 1080)
        val display = iView.getDisplay()
        val point = Point()
        display.getRealSize(point)
        val displayWidth = minOf(point.x, point.y)
        val displayHeight = maxOf(point.x, point.y)
        val displayRatio = displayHeight.div(displayWidth.toFloat())
        val previewRatio = size?.height?.toFloat() ?: 1920.div(size?.width?.toFloat() ?: 1080.toFloat())

        if (displayRatio > previewRatio) {
            val surfaceParams = iView.getSurfaceView().layoutParams
            surfaceParams.height = (point.y / displayRatio * previewRatio).toInt()
            iView.getSurfaceView().layoutParams = surfaceParams
        }

        val pictureSize = mCamera?.parameters?.supportedPictureSizes?.maxBy { it.width.times(it.height) }
        param?.setPictureSize(pictureSize?.width ?: 1080, pictureSize?.height ?: 1920)
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            param?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            Log.d(TAG, "enabling autofocus")
        } else {
            Log.d(TAG, "autofocus not available")
        }
        param?.flashMode = Camera.Parameters.FLASH_MODE_AUTO

        mCamera?.parameters = param
        mCamera?.setDisplayOrientation(90)

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        updateCamera()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        synchronized(this) {
            mCamera?.stopPreview()
            mCamera?.setPreviewCallback(null)
            mCamera?.release()
            mCamera = null
        }
    }

    override fun onPictureTaken(p0: ByteArray?, p1: Camera?) {
        Log.i(TAG, "on picture taken")
        Observable.just(p0)
                .subscribeOn(proxySchedule)
                .subscribe {
                    val pictureSize = p1?.parameters?.pictureSize

                    val mat = Mat(Size(pictureSize?.width?.toDouble() ?: 1080.toDouble(),
                            pictureSize?.height?.toDouble() ?: 1920.toDouble()), CvType.CV_8U)
                    mat.put(0, 0, p0)
                    val pic = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
                    Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE)
                    mat.release()
                    SourceManager.corners = processPicture(pic)
                    SourceManager.pic = pic
                    context.startActivity(Intent(context, CropActivity::class.java))
                    busy = false
                }
    }


    override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
        if (busy) {
            return
        }
        Log.i(TAG, "on process start")
        busy = true
        Observable.just(p0)
                .observeOn(proxySchedule)
                .subscribe(Action1 {
                    Log.i(TAG, "on process: " + Thread.currentThread().name)
                    Log.i(TAG, "start prepare paper")
                    val parameters = p1?.parameters
                    val width = parameters?.previewSize?.width
                    val height = parameters?.previewSize?.height
                    val yuv = YuvImage(p0, parameters?.previewFormat ?: 0, width ?: 1080, height ?: 1920, null)
                    val out = ByteArrayOutputStream()
                    yuv.compressToJpeg(Rect(0, 0, width ?: 1080, height ?: 1920), 100, out)
                    val bytes = out.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val img = Mat()
                    Utils.bitmapToMat(bitmap, img)
                    bitmap.recycle()
                    Core.rotate(img, img, Core.ROTATE_90_CLOCKWISE)
                    try {
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    Log.i(TAG, "on process start")
                    val corners = processPicture(img)
                    busy = false
                    Log.i(TAG, "on process complete: " + corners?.toString())

                    Observable.just(corners)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Action1 {
                                Log.i(TAG, "draw paper rect")
                                iView.getPaperRect().onCornersDetected(corners)
                            })
                })
    }

    private fun getMaxResolution(): Camera.Size? = mCamera?.parameters?.supportedPreviewSizes?.maxBy { it.width }

    private fun updateCamera() {
        if (null == mCamera) {
            return
        }
        mCamera?.stopPreview()
        try {
            mCamera?.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        mCamera?.setPreviewCallback(this)
        mCamera?.startPreview()
    }


}