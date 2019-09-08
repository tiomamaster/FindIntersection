package com.gmailtiomamaster.findintersection

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = DrawingView(this)
        setContentView(v)
    }
}

class DrawingView(context: Context) : View(context) {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
    }
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private val path = Path()
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.MITER
        strokeWidth = 4f
    }
    private val circlePath = Path()

    private var prevX = 0f
    private var prevY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
        canvas.drawPath(path, paint)
        canvas.drawPath(circlePath, circlePaint)
    }

    private var pts = PointsList()

    private fun touchStart(x: Float, y: Float) {
        Log.d(TAG, "touchStart")
        pts = PointsList()

        path.reset()
        path.moveTo(x, y)
        prevX = x
        prevY = y
    }

    private fun touchMove(x: Float, y: Float) {
        Log.d(TAG, "touchMove")
        if (pts.add(PointsList.Pt(x, y))) {
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//                invalidate()
//                canvas.drawPath(pts.asPath, paint)
//                return
        }
        val dx = Math.abs(x - prevX)
        val dy = Math.abs(y - prevY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(prevX, prevY, (x + prevX) / 2, (y + prevY) / 2)

            prevX = x
            prevY = y

            circlePath.reset()
            circlePath.addCircle(prevX, prevY, 30f, Path.Direction.CW)
        }
    }

    private fun touchUp() {
        Log.d(TAG, "touchUp")
//        path.lineTo(prevX, prevY)
        circlePath.reset()
        // commit the path to our offscreen
//        path.close()
//        canvas.drawPath(path, paint)
        // kill this so we don't double draw
        path.reset()

//        intersection = with(pts) {
//            val p1 = last
//            val p2 = first
//            for (i in lastIndex - 1 downTo 1) {
//                val p3 = get(i)
//                val p4 = get(i - 1)
//                if (segmentsIntersect(p1, p2, p3, p4)) {
//                    Log.d(TAG, "intersection")
//                    canvas!!.drawCircle(x, y, 15f, circlePaint)
//                    return@with i + 1
//                }
//            }
//            -1
//        }

//        val interpolatedPath = Path().apply {
//            moveTo(pts[intersection].x, pts[intersection].y)
//            for (i in intersection until pts.lastIndex) {
//                val (sX, sY) = pts[i].x to pts[i].y
//                val (eX, eY) = pts[i + 1].x to pts[i + 1].y
//                quadTo(sX, sY, (sX + eX) / 2, (sY + eY) / 2)
//            }
//            close()
//        }
        pts.close()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawPath(pts.asPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (pts.haveIntersection) {
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//            invalidate()
//            return true
//        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!pts.haveIntersection) {
                    touchMove(x, y)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private companion object {
        const val TOUCH_TOLERANCE = 5f
    }
}

class PointsList(private val maxSize: Int = 200) : LinkedList<PointsList.Pt>() {

    data class Pt(val x: Float, val y: Float)

    override fun add(element: Pt): Boolean {
        return if (size == maxSize) {
            val i = Random().nextInt(maxSize)
            removeAt(i)
            checkIntersection(false)
            super.add(element)
        } else {
            checkIntersection(false)
            super.add(element)
        }
    }

    private fun checkIntersection(close: Boolean) {
        if (size > 3 && indexOfIntersection == -1) {
            val p1 = if (close) last else get(lastIndex - 1)
            val p2 = if (close) first else last
            val from = if (close) lastIndex - 1 else lastIndex - 3
            for (i in from downTo 1) {
                val p3 = get(i - 1)
                val p4 = get(i)
                if (segmentsIntersect(p1, p2, p3, p4)) {
                    Log.d(TAG, "intersection")
                    indexOfIntersection = i + 1
                    break
                }
            }
        }
    }

    //        intersection = with(pts) {
//            val p1 = last
//            val p2 = first
//            for (i in lastIndex - 1 downTo 1) {
//                val p3 = get(i)
//                val p4 = get(i - 1)
//                if (segmentsIntersect(p1, p2, p3, p4)) {
//                    Log.d(TAG, "intersection")
//                    canvas!!.drawCircle(x, y, 15f, circlePaint)
//                    return@with i + 1
//                }
//            }
//            -1
//        }

    fun close() {
        checkIntersection(true)
    }

    private var indexOfIntersection: Int = -1

    val haveIntersection: Boolean
        get() = indexOfIntersection != -1

    val asPath
        get() = Path().apply {
            moveTo(get(indexOfIntersection).x, get(indexOfIntersection).y)
            for (i in indexOfIntersection until lastIndex) {
                val pS = get(i)
                val (sX, sY) = pS.x to pS.y
                val pE = get(i + 1)
                val (eX, eY) = pE.x to pE.y
                quadTo(sX, sY, (sX + eX) / 2, (sY + eY) / 2)
            }
            close()
        }

    // Finds the orientation of point 'c' relative to the line segment (a, b)
    // Returns  0 if all three points are collinear.
    // Returns -1 if 'c' is clockwise to segment (a, b), i.e right of line formed by the segment.
    // Returns +1 if 'c' is counter clockwise to segment (a, b), i.e left of line
    // formed by the segment.
    private fun orientation(a: Pt, b: Pt, c: Pt): Int {
        val value = (b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y)
        if (abs(value) < EPS) return 0
        return if (value > 0) -1 else +1
    }

    // Tests whether point 'c' is on the line segment (a, b).
    // Ensure first that point c is collinear to segment (a, b) and
    // then check whether c is within the rectangle formed by (a, b)
    private fun pointOnLine(a: Pt, b: Pt, c: Pt): Boolean {
        return orientation(a, b, c) == 0 &&
                min(a.x, b.x) <= c.x && c.x <= max(a.x, b.x) &&
                min(a.y, b.y) <= c.y && c.y <= max(a.y, b.y)
    }

    // Determines whether two segments intersect.
    private fun segmentsIntersect(p1: Pt, p2: Pt, p3: Pt, p4: Pt): Boolean {

        // Get the orientation of points p3 and p4 in relation
        // to the line segment (p1, p2)
        val o1 = orientation(p1, p2, p3)
        val o2 = orientation(p1, p2, p4)
        val o3 = orientation(p3, p4, p1)
        val o4 = orientation(p3, p4, p2)

        // If the points p1, p2 are on opposite sides of the infinite
        // line formed by (p3, p4) and conversly p3, p4 are on opposite
        // sides of the infinite line formed by (p1, p2) then there is
        // an haveIntersection.
        if (o1 != o2 && o3 != o4) return true

        // Collinear special cases (perhaps these if checks can be simplified?)
        if (o1 == 0 && pointOnLine(p1, p2, p3)) return true
        if (o2 == 0 && pointOnLine(p1, p2, p4)) return true
        if (o3 == 0 && pointOnLine(p3, p4, p1)) return true
        return o4 == 0 && pointOnLine(p3, p4, p2)
    }

    private companion object {
        const val EPS = 1e-5
    }
}