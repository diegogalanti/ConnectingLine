package com.gallardo.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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
        color = Color.RED
        strokeWidth = 4F
    }

    private val set = ConstraintSet()

    private var dentSize = 20.0F

    private var radiusCornerEffect = 5.0F

    var preferredPath = SIDE_TO_SIDE

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val originView = rootView.findViewById<View>(idOriginView)
        val destinationView = rootView.findViewById<View>(idDestinationView)
        val constraintLayout = parent as ConstraintLayout

        adjustConstraintSet(originView, destinationView)
        set.applyTo(constraintLayout)
        paint.pathEffect = CornerPathEffect(radiusCornerEffect)
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
        return when(preferredPath){
            SIDE_TO_SIDE -> {
                if (isSideToSidePossible(originView, destinationView))
                    createPathSideToSide(originView, destinationView)
                else if (isTopToBottomPossible(originView, destinationView))
                    createPathTopToBottom(originView, destinationView)
                else
                    Path()
            }
            else -> {
                if (isTopToBottomPossible(originView, destinationView))
                    createPathTopToBottom(originView, destinationView)
                else if (isSideToSidePossible(originView, destinationView))
                    createPathSideToSide(originView, destinationView)
                else
                    Path()
            }
        }
    }

    private fun createPathSideToSide(originView: View, destinationView: View): Path {
        val optimalPath = Path()
        val distLeftRight = abs(originView.left - destinationView.right)
        val distRightLeft = abs(originView.right - destinationView.left)
        val verticalDist = abs((originView.top + (originView.height / 2)) - (destinationView.top + (destinationView.height / 2))).toFloat()

        val arrHorizontalDist = listOf(distLeftRight, distRightLeft)

        if(isAbove(originView, destinationView))
            when (arrHorizontalDist.min()) {
                distLeftRight -> {
                    optimalPath.moveTo(
                        destinationView.width.toFloat() + distLeftRight,
                        originView.height.toFloat() / 2
                    )
                    optimalPath.rLineTo(-dentSize, 0.0F)
                    optimalPath.rLineTo(0.0F, verticalDist)
                    optimalPath.lineTo(
                        destinationView.width.toFloat(),
                        originView.height.toFloat() / 2 + verticalDist
                    )
                }
                distRightLeft -> {
                    optimalPath.moveTo(
                        originView.width.toFloat(),
                        originView.height.toFloat() / 2)
                    optimalPath.rLineTo(dentSize, 0.0F)
                    optimalPath.rLineTo(0.0F, verticalDist)
                    optimalPath.lineTo(
                        originView.width.toFloat() + distRightLeft.toFloat(),
                        originView.height.toFloat() / 2 + verticalDist
                    )
                }
            }
        else
            when (arrHorizontalDist.min()) {
                distLeftRight -> {
                    optimalPath.moveTo(
                        destinationView.width.toFloat() + distLeftRight,
                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist//qualquer coisa ajustar aqui segundo para origin
                    )
                    optimalPath.rLineTo(-dentSize, 0.0F)
                    optimalPath.rLineTo(0.0F, -verticalDist)
                    optimalPath.lineTo(
                        destinationView.width.toFloat(),
                        destinationView.height.toFloat() / 2
                    )
                }
                distRightLeft -> {
                    optimalPath.moveTo(
                        originView.width.toFloat(),
                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist //qualquer coisa ajustar aqui segundo para origin
                    )
                    optimalPath.rLineTo(dentSize, 0.0F)
                    optimalPath.rLineTo(0.0F, -verticalDist)
                    optimalPath.lineTo(
                        originView.width.toFloat() + distRightLeft,
                        destinationView.height.toFloat() / 2
                    )
                }
            }
        return optimalPath
    }

    private fun createPathTopToBottom(originView: View, destinationView: View): Path {
        val optimalPath = Path()
        val distTopBottom = abs(originView.top - destinationView.bottom)
        val distBottomTop = abs(originView.bottom - destinationView.top)
        val horizontalDist = abs((originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2))).toFloat()

        val arrVerticalDist = listOf(distTopBottom, distBottomTop)

        if(isLeft(originView, destinationView))
            when (arrVerticalDist.min()) {
                distTopBottom -> {
                    optimalPath.moveTo(
                        originView.width.toFloat() / 2,
                        destinationView.height.toFloat() + distTopBottom
                    )
                    optimalPath.rLineTo(0.0F, -dentSize)
                    optimalPath.rLineTo(horizontalDist, 0.0F)
                    optimalPath.lineTo(
                        originView.width.toFloat() / 2 + horizontalDist,
                        destinationView.height.toFloat()
                    )
                }
                distBottomTop -> {
                    optimalPath.moveTo(
                        originView.width.toFloat() / 2,
                        originView.height.toFloat())
                    optimalPath.rLineTo(0.0F, dentSize)
                    optimalPath.rLineTo(horizontalDist, 0.0F)
                    optimalPath.lineTo(
                        originView.width.toFloat() / 2 + horizontalDist,
                        originView.height.toFloat() + distBottomTop.toFloat()
                    )
                }
            }
        else
            when (arrVerticalDist.min()) {
                distTopBottom -> {
                    optimalPath.moveTo(
                        destinationView.width.toFloat() - destinationView.width / 2 + horizontalDist,
                        destinationView.height.toFloat() + distTopBottom
                    )
                    optimalPath.rLineTo(0.0F, -dentSize)
                    optimalPath.rLineTo(-horizontalDist, 0.0F)
                    optimalPath.lineTo(
                        destinationView.width.toFloat() / 2,
                        destinationView.height.toFloat()
                    )
                }
                distBottomTop -> {
                    optimalPath.moveTo(
                        destinationView.width.toFloat() - destinationView.width / 2 + horizontalDist,
                        originView.height.toFloat()
                    )
                    optimalPath.rLineTo(0.0F, dentSize)
                    optimalPath.rLineTo(-horizontalDist, 0.0F)
                    optimalPath.lineTo(
                        destinationView.width.toFloat() / 2,
                        originView.height.toFloat() + distBottomTop
                    )
                }
            }
        return optimalPath
    }

    private fun isSideToSidePossible(originView : View, destinationView : View): Boolean {
        return originView.right < destinationView.left || originView.left > destinationView.right
    }

    private fun isTopToBottomPossible(originView : View, destinationView : View): Boolean {
        return originView.bottom < destinationView.top || originView.top > destinationView.bottom
    }

    private fun isAbove(originView : View, destinationView : View) = (originView.top + (originView.height / 2)) - (destinationView.top + (destinationView.height / 2)) < 0

    private fun isLeft(originView : View, destinationView : View) = (originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2)) < 0

    companion object PathDirection{
        val SIDE_TO_SIDE = 0;
        val TOP_TO_BOTTOM = 1;
    }
}