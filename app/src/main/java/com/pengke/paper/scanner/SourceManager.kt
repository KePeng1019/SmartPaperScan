package com.pengke.paper.scanner


import com.pengke.paper.scanner.processor.Corners
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size

class SourceManager {
    companion object {
        var pic: Mat? = null
        var corners: Corners? = null
    }
}