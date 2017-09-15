package com.pengke.paper.scanner.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import org.opencv.core.Point
import android.view.View
import com.pengke.paper.scanner.SourceManager
import com.pengke.paper.scanner.processor.Corners
import org.opencv.core.Size


class PaperRectangle : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defTheme: Int) : super(context, attributes, defTheme)

    private val rectPaint = Paint()
    private var ratio: Double = 1.0
    private var tl: Point = Point()
    private var tr: Point = Point()
    private var br: Point = Point()
    private var bl: Point = Point()
    private val path: Path = Path()

    init {
        rectPaint.color = Color.BLUE
        rectPaint.isAntiAlias = true
        rectPaint.isDither = true
        rectPaint.strokeWidth = 6.toFloat()
        rectPaint.style = Paint.Style.STROKE
    }

    fun onCornersDetected(corners: Corners?) {
        if (corners == null) {
            path.reset()
            invalidate()
            return
        }
        tl = corners.corners[0] ?: Point()
        tr = corners.corners[1] ?: Point()
        br = corners.corners[2] ?: Point()
        bl = corners.corners[3] ?: Point()

        path.reset()
        path.moveTo(tl.x.toFloat(), tl.y.toFloat())
        path.lineTo(tr.x.toFloat(), tr.y.toFloat())
        path.lineTo(br.x.toFloat(), br.y.toFloat())
        path.lineTo(bl.x.toFloat(), bl.y.toFloat())
        path.close()
        invalidate()
    }

    fun onCorners2Crop(corners: Corners?, size: Size?) {

        tl = corners?.corners?.get(0) ?: SourceManager.defaultTl
        tr = corners?.corners?.get(1) ?: SourceManager.defaultTr
        br = corners?.corners?.get(2) ?: SourceManager.defaultBr
        bl = corners?.corners?.get(3) ?: SourceManager.defaultBl
        ratio = size?.width?.div(1080.0) ?: 1.0
        resize()
        path.reset()
        path.moveTo(tl.x.toFloat(), tl.y.toFloat())
        path.lineTo(tr.x.toFloat(), tr.y.toFloat())
        path.lineTo(br.x.toFloat(), br.y.toFloat())
        path.lineTo(bl.x.toFloat(), bl.y.toFloat())
        path.close()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(path, rectPaint)
    }

    private fun resize() {
        tl.x = tl.x.div(ratio)
        tl.y = tl.y.div(ratio)
        tr.x = tr.x.div(ratio)
        tr.y = tr.y.div(ratio)
        br.x = br.x.div(ratio)
        br.y = br.y.div(ratio)
        bl.x = bl.x.div(ratio)
        bl.y = bl.y.div(ratio)
    }
}