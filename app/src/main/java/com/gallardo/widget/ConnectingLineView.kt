package com.gallardo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.lang.Math.abs


class ConnectingLineView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private val idOriginView: Int = with(attrs) {
        for (iterator in 0..attributeCount) {
            return@with if (attrs.getAttributeNameResource(iterator) == R.attr.originView)
                attrs.getAttributeResourceValue(iterator, 0)
            else
                continue
        }
        return@with 0
    }

    private val idDestinationView: Int = with(attrs) {
        for (iterator in 0..attributeCount) {
            return@with if (attrs.getAttributeNameResource(iterator) == R.attr.destinationView)
                attrs.getAttributeResourceValue(iterator, 0)
            else
                continue
        }
        return@with 0
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2F
    }

    private val set = ConstraintSet()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val originView = rootView.findViewById<View>(idOriginView)
        val destinationView = rootView.findViewById<View>(idDestinationView)
        val constraintLayout = parent as ConstraintLayout

        adjustConstraintSet(originView, destinationView)
        set.applyTo(constraintLayout)
        canvas.drawPath(createPath(originView, destinationView), paint)
    }

    private fun adjustConstraintSet(originView: View, destinationView: View) {
        set.connect(
            id,
            ConstraintSet.LEFT,
            if (originView.left < destinationView.left) idOriginView else idDestinationView,
            ConstraintSet.LEFT,
            0
        )
        set.connect(
            id,
            ConstraintSet.TOP,
            if (originView.top < destinationView.top) idOriginView else idDestinationView,
            ConstraintSet.TOP,
            0
        )
        set.connect(
            id,
            ConstraintSet.RIGHT,
            if (destinationView.right > originView.right) idDestinationView else idOriginView,
            ConstraintSet.RIGHT,
            0
        )
        set.connect(
            id,
            ConstraintSet.BOTTOM,
            if (destinationView.bottom > originView.bottom) idDestinationView else idOriginView,
            ConstraintSet.BOTTOM,
            0
        )
    }

    private fun createPath(originView: View, destinationView: View): Path {
        val optimalPath = Path()
        val distLeftRight = abs(originView.left - destinationView.right)
        val distRightLeft = abs(originView.right - destinationView.left)
        val distTopBottom = abs(originView.top - destinationView.bottom)
        val distBottomTop = abs(originView.bottom - destinationView.top)

        val arrHorizontalDist = listOf(distLeftRight, distRightLeft)
        val arrVerticalDist = listOf(distTopBottom, distBottomTop)

        when (arrVerticalDist.min()) {
            distBottomTop ->
                when (arrHorizontalDist.min()) {
                    distLeftRight -> {
                        optimalPath.moveTo(
                            destinationView.width.toFloat() + distLeftRight,
                            originView.height.toFloat()
                        )
                        optimalPath.lineTo(
                            destinationView.width.toFloat(),
                            originView.height.toFloat() + distBottomTop
                        )
                    }
                    distRightLeft -> {
                        optimalPath.moveTo(
                            originView.width.toFloat(),
                            originView.height.toFloat())
                        optimalPath.lineTo(
                            originView.width.toFloat() + distRightLeft.toFloat(),
                            originView.height.toFloat() + distBottomTop
                        )
                    }
                }
            distTopBottom ->
                when (arrHorizontalDist.min()) {
                    distLeftRight -> {
                        optimalPath.moveTo(
                            destinationView.width.toFloat() + distLeftRight,
                            destinationView.height.toFloat() + distTopBottom
                        )
                        optimalPath.lineTo(
                            destinationView.width.toFloat(),
                            destinationView.height.toFloat()
                        )
                    }
                    distRightLeft -> {
                        optimalPath.moveTo(
                            originView.width.toFloat(),
                            destinationView.height.toFloat() + distTopBottom
                        )
                        optimalPath.lineTo(
                            originView.width.toFloat() + distRightLeft,
                            destinationView.height.toFloat()
                        )
                    }
                }
        }
        return optimalPath
    }
}