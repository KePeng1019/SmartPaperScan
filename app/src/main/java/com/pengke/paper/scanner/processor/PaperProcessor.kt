package com.pengke.paper.scanner.processor

import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.collections.ArrayList

val TAG: String = "PaperProcessor"

fun processPicture(previewFrame: Mat): Corners? {
    val contours = findContours(previewFrame)
    return getCorners(contours, previewFrame.size())
}


private fun findContours(src: Mat): ArrayList<MatOfPoint> {

    val grayImage: Mat
    val cannedImage: Mat
    val kernel: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0))
    val dilate: Mat
    val size = Size(src.size().width, src.size().height)
    grayImage = Mat(size, CvType.CV_8UC4)
    cannedImage = Mat(size, CvType.CV_8UC1)
    dilate = Mat(size, CvType.CV_8UC1)

    Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY)
    Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
    Imgproc.threshold(grayImage, grayImage, 20.0, 255.0, Imgproc.THRESH_TRIANGLE)
    Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
    Imgproc.dilate(cannedImage, dilate, kernel)
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(dilate, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
    contours.sortByDescending { p: MatOfPoint -> Imgproc.contourArea(p) }
    hierarchy.release()
    grayImage.release()
    cannedImage.release()
    kernel.release()
    dilate.release()

    return contours
}

private fun getCorners(contours: ArrayList<MatOfPoint>, size: Size): Corners? {
    val indexTo: Int
    when (contours.size) {
        in 0..5 -> indexTo = contours.size - 1
        else -> indexTo = 4
    }
    for (index in 0..contours.size) {
        if (index in 0..indexTo) {
            val c2f = MatOfPoint2f(*contours[index].toArray())
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
            val points = approx.toArray().asList()
            Log.i(TAG, "point size: " + points.size)
            // select biggest 4 angles polygon
            if (points.size >= 4) {
                val foundPoints = sortPoints(points)
                if (insideArea(foundPoints, size)) {
                    return Corners(foundPoints, size)
                }
            }
        } else {
            return null
        }
    }
//    for (p in contours) {
//        when (contours.indexOf(p)) {
//            in 0..5 -> {
//                val c2f = MatOfPoint2f(*p.toArray())
//                val peri = Imgproc.arcLength(c2f, true)
//                val approx = MatOfPoint2f()
//                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
//                val points = approx.toArray().asList()
//                Log.i(TAG, "point size: " + points.size)
//                // select biggest 4 angles polygon
//                if (points.size >= 4) {
//                    val foundPoints = sortPoints(points)
//                    if (insideArea(foundPoints, size)) {
//                        return Corners(foundPoints, size)
//                    }
//                }
//            }
//            else -> {
//                return null
//            }
//        }
//    }
    return null
}

private fun sortPoints(points: List<Point>): List<Point> {
    val p0 = points.minBy { point -> point.x + point.y } ?: Point()
    val p1 = points.maxBy { point -> point.x - point.y } ?: Point()
    val p2 = points.maxBy { point -> point.x + point.y } ?: Point()
    val p3 = points.minBy { point -> point.x - point.y } ?: Point()

    return listOf(p0, p1, p2, p3)
}


private fun insideArea(rp: List<Point>, size: Size): Boolean {

    val width = java.lang.Double.valueOf(size.width)!!.toInt()
    val height = java.lang.Double.valueOf(size.height)!!.toInt()
    val baseHeightMeasure = height / 8
    val baseWidthMeasure = width / 8

    val bottomPos = height / 2 + baseHeightMeasure
    val topPos = height / 2 - baseHeightMeasure
    val leftPos = width / 2 - baseWidthMeasure
    val rightPos = width / 2 + baseWidthMeasure

    return rp[0].x <= leftPos && rp[0].y <= topPos
            && rp[1].x >= rightPos && rp[1].y <= topPos
            && rp[2].x >= rightPos && rp[2].y >= bottomPos
            && rp[3].x <= leftPos && rp[3].y >= bottomPos
}