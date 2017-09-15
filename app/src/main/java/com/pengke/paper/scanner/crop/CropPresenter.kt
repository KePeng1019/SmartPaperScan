package com.pengke.paper.scanner.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.pengke.paper.scanner.SourceManager
import com.pengke.paper.scanner.processor.Corners
import org.opencv.android.Utils

import org.opencv.core.Mat


class CropPresenter {
    private val context: Context
    private val iCropView: ICropView.Proxy
    private val picture: Mat? = SourceManager.pic
    private val corners: Corners? = SourceManager.corners
    private var croppedPicture: Mat? = null
    private var enhancePicture: Mat? = null
    private var blackPicture: Mat? = null

    constructor(context: Context, iCropView: ICropView.Proxy) {
        this.context = context
        this.iCropView = iCropView
        iCropView.getPaperRect().onCorners2Crop(corners, picture?.size())
        val bitmap = Bitmap.createBitmap(picture?.width() ?: 1080, picture?.height() ?: 1920, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(picture, bitmap, true)
        iCropView.getPaper().setImageBitmap(bitmap)
    }

    fun crop() {

    }

    fun enhance() {

    }

    fun black() {

    }

    fun reset() {

    }

    fun save() {

    }
}