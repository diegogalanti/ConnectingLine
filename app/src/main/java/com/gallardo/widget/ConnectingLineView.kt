package com.gallardo.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.withStyledAttributes
import kotlin.math.abs


class ConnectingLineView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var idOriginView: Int = 0
    private var idDestinationView: Int = 0
    private lateinit var originView: View
    private lateinit var destinationView: View
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private var isFirstDraw = true

    private var dentSize = 20f

    private var preferredPath = SIDE_TO_SIDE

    init {
        minimumHeight = 1 //required when the constraintLayout is inside a ScrollView
        context.withStyledAttributes(attrs, R.styleable.ConnectingLineView) {
            idOriginView = getResourceId(R.styleable.ConnectingLineView_originView, 0)
            idDestinationView = getResourceId(R.styleable.ConnectingLineView_destinationView, 0)
            dentSize = getFloat(R.styleable.ConnectingLineView_dentSize, dentSize)
            preferredPath = getInt(R.styleable.ConnectingLineView_preferredPath, SIDE_TO_SIDE)
            paint.color = getColor(R.styleable.ConnectingLineView_lineColor, Color.BLACK)
            paint.strokeWidth = getFloat(R.styleable.ConnectingLineView_lineWidth, 2f)
            paint.pathEffect =
                CornerPathEffect(getFloat(R.styleable.ConnectingLineView_cornerEffectRadius, 0f))
            try {
                paint.setShadowLayer(
                    2f,
                    1f,
                    1f,
                    getColorOrThrow(R.styleable.ConnectingLineView_shadowColor)
                )
            } catch (e: IllegalArgumentException) {
                Log.v("ConnectingLine", "No shadow selected")
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        originView = (parent as ConstraintLayout).findViewById(idOriginView)
        destinationView = (parent as ConstraintLayout).findViewById(idDestinationView)
    }

    /**
     * We adjust the constraints here because we need to know which view, origin or destination
     * is more to the left, right, top and bottom. The problem is that onLayout comes after onMeasure
     * So we already calculated our size wrongly at this point, as we have not constraint here.
     *
     * Calling requestLayout() here does not work so we implemented a isFirstDraw var that adjust
     * the constraint on the first time, please check onDraw for further explanation
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (isFirstDraw)
            adjustConstraints()
    }

    /**
     * On the first call to onDraw we don't draw, instead we refresh the layoutParams so the lifecycle
     * go back to onMeasure with the correct data. On the further calls, the path is drawn normally.
     *
     * Check onLayout for further explanation
     */
    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            layoutParams = layoutParams
            isFirstDraw = false
            return
        }
        super.onDraw(canvas)
        canvas.drawPath(createPath(), paint)
    }

    private fun adjustConstraints() {
        val constraint = ConstraintSet.Constraint()
        constraint.layout.leftToLeft =
            if (originView.left < destinationView.left) idOriginView else idDestinationView
        constraint.layout.rightToRight =
            if (destinationView.right > originView.right) idDestinationView else idOriginView
        constraint.layout.topToTop =
            if (originView.top < destinationView.top) idOriginView else idDestinationView
        constraint.layout.bottomToBottom =
            if (destinationView.bottom > originView.bottom) idDestinationView else idOriginView

        constraint.applyTo(this.layoutParams as ConstraintLayout.LayoutParams)
    }

    private fun createPath(): Path {
        return when (preferredPath) {
            SIDE_TO_SIDE -> {
                if (isSideToSidePossible())
                    createPathSideToSide()
                else if (isTopToBottomPossible())
                    createPathTopToBottom()
                else
                    Path()
            }
            else -> {
                if (isTopToBottomPossible())
                    createPathTopToBottom()
                else if (isSideToSidePossible())
                    createPathSideToSide()
                else
                    Path()
            }
        }
    }

    private fun createPathSideToSide(): Path {
        val optimalPath = Path()
        val distLeftRight = abs(originView.left - destinationView.right)
        val distRightLeft = abs(originView.right - destinationView.left)
        val verticalDist =
            abs((originView.top + (originView.height / 2)) - (destinationView.top + (destinationView.height / 2))).toFloat()

        val arrHorizontalDist = listOf(distLeftRight, distRightLeft)

        if (isAbove())
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
                        originView.height.toFloat() / 2
                    )
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
                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist
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
                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist
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

    private fun createPathTopToBottom(): Path {
        val optimalPath = Path()
        val distTopBottom = abs(originView.top - destinationView.bottom)
        val distBottomTop = abs(originView.bottom - destinationView.top)
        val horizontalDist =
            abs((originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2))).toFloat()

        val arrVerticalDist = listOf(distTopBottom, distBottomTop)

        if (isLeft())
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
                        originView.height.toFloat()
                    )
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

    private fun isSideToSidePossible(): Boolean {
        return originView.right < destinationView.left || originView.left > destinationView.right
    }

    private fun isTopToBottomPossible(): Boolean {
        return originView.bottom < destinationView.top || originView.top > destinationView.bottom
    }

    private fun isAbove() =
        (originView.top + (originView.height / 2)) - (destinationView.top + (destinationView.height / 2)) < 0

    private fun isLeft() =
        (originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2)) < 0

    companion object PathDirection {
        const val SIDE_TO_SIDE = 0
        const val TOP_TO_BOTTOM = 1
    }
}