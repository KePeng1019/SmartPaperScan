package com.pengke.paper.scanner.scan

import android.view.Display
import android.view.SurfaceView
import com.pengke.paper.scanner.view.PaperRectangle

interface IScanView {
    interface Proxy {
        fun exit()
        fun getDisplay(): Display
        fun getSurfaceView(): SurfaceView
        fun getPaperRect(): PaperRectangle
    }
}