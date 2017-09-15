package com.pengke.paper.scanner.scan

import android.util.Log
import android.view.Display
import android.view.SurfaceView
import com.pengke.paper.scanner.R
import com.pengke.paper.scanner.base.BaseActivity
import com.pengke.paper.scanner.view.PaperRectangle

import kotlinx.android.synthetic.main.activity_scan.*
import org.opencv.android.OpenCVLoader

class ScanActivity : BaseActivity(), IScanView.Proxy {

    private var mPresenter: ScanPresenter? = null


    override fun provideContentViewId(): Int = R.layout.activity_scan

    override fun initPresenter() {
        mPresenter = ScanPresenter(this, this)
    }

    override fun prepare() {
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "loading opencv error, exit")
            finish()
        }
        shut.setOnClickListener {
            Log.i(TAG, "shut clicked")
            mPresenter?.shut() }
    }


    override fun onStart() {
        super.onStart()
        mPresenter?.start()
    }

    override fun onStop() {
        super.onStop()
        mPresenter?.stop()
    }

    override fun exit() {
        finish()
    }

    override fun getDisplay(): Display = windowManager.defaultDisplay

    override fun getSurfaceView(): SurfaceView = surface

    override fun getPaperRect(): PaperRectangle = paper_rect
}