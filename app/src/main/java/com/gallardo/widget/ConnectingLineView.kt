package com.gallardo.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.withStyledAttributes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ConnectingLineView : View {

    constructor(context: Context, idOriginView: Int, idDestinationView: Int) : super(context) {
        this.idOriginView = idOriginView
        this.idDestinationView = idDestinationView
    }

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.withStyledAttributes(attrs, R.styleable.ConnectingLineView) {
            idOriginView =
                getResourceId(R.styleable.ConnectingLineView_originView, 0)
            idDestinationView =
                getResourceId(R.styleable.ConnectingLineView_destinationView, 0)
            dentSize = getInt(R.styleable.ConnectingLineView_dentSize, dentSize)
            preferredPath = getInt(R.styleable.ConnectingLineView_preferredPath, preferredPath)
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

    private var idOriginView: Int = 0
    private var idDestinationView: Int = 0
    private lateinit var originView: View
    private lateinit var destinationView: View
    var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
        set(value) {
            value.style = Paint.Style.STROKE //if the passed Paint has no style or the wrong style
            field = value
        }
    private var isFirstDraw = true

    var dentSize = 20

    var preferredPath = TOP_TO_BOTTOM

    init {
        minimumHeight = 1 //required when the constraintLayout is inside a ScrollView
    }

    fun setOriginView(newOriginViewId: Int): ConnectingLineView {
        idOriginView = newOriginViewId
        originView = (parent as ViewGroup).findViewById(idOriginView)
        return this
    }

    fun setDestinationView(newDestinationViewId: Int): ConnectingLineView {
        idDestinationView = newDestinationViewId
        destinationView = (parent as ViewGroup).findViewById(idDestinationView)
        return this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        originView = (parent as ViewGroup).findViewById(idOriginView)
        destinationView = (parent as ViewGroup).findViewById(idDestinationView)
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
        layoutParams = layoutParams
        super.onDraw(canvas)
        canvas.drawPath(createPath2(), paint)
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

        constraint.layout.bottomMargin = -dentSize - (paint.strokeWidth / 2).toInt()
        constraint.layout.topMargin = -dentSize - (paint.strokeWidth / 2).toInt()
        constraint.layout.endMargin = -dentSize - (paint.strokeWidth / 2).toInt()
        constraint.layout.startMargin = -dentSize - (paint.strokeWidth / 2).toInt()
        constraint.applyTo(this.layoutParams as ConstraintLayout.LayoutParams)
    }

//    private fun createPath(): Path {
//        return when (preferredPath) {
//            VERTICAL -> {
//                if (isSideToSidePossible())
//                    createPathSideToSide()
//                else if (isTopToBottomPossible())
//                    createPathTopToBottom()
//                else
//                    Path()
//            }
//            TOP_TO_TOP -> {
//                createPathTopToTop()
//            }
//            else -> {
//                if (isTopToBottomPossible())
//                    createPathTopToBottom()
//                else if (isSideToSidePossible())
//                    createPathSideToSide()
//                else
//                    Path()
//            }
//        }
//    }

    private fun createPath2(): Path {
        val path = Path()

        moveToInitialXY(path)
        applyFirstLine(path)
        applySecondLine(path)
        applyThirdLine(path)
        applyFourthLine(path)
        applyFifthLine(path)
        applyMarginOffset(path)
        return path
    }

    private fun moveToInitialXY(path: Path) {
        path.moveTo(
            when (preferredPath) {
                TOP_TO_TOP, TOP_TO_RIGHT, TOP_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP, BOTTOM_TO_RIGHT, BOTTOM_TO_LEFT, BOTTOM_TO_BOTTOM -> {
                    if (leftToLeftDistance() >= 0)
                        (originView.width / 2).toFloat()
                    else
                        (originView.left - destinationView.left + (originView.width / 2)).toFloat()
                }
                LEFT_TO_TOP, LEFT_TO_RIGHT, LEFT_TO_LEFT, LEFT_TO_BOTTOM -> {
                    if (leftToLeftDistance() >= 0)
                        0f
                    else
                        (originView.left - destinationView.left).toFloat()
                }
                RIGHT_TO_TOP, RIGHT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_BOTTOM -> {
                    if (leftToLeftDistance() >= 0)
                        originView.width.toFloat()
                    else
                        (originView.left - destinationView.left + originView.width).toFloat()
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                LEFT_TO_TOP, LEFT_TO_RIGHT, LEFT_TO_LEFT, LEFT_TO_BOTTOM, RIGHT_TO_TOP, RIGHT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_BOTTOM -> {
                    if (topToTopDistance() >= 0)
                        (originView.height / 2).toFloat()
                    else
                        (originView.top - destinationView.top + (originView.height / 2)).toFloat()
                }
                TOP_TO_TOP, TOP_TO_RIGHT, TOP_TO_LEFT, TOP_TO_BOTTOM -> {
                    if (topToTopDistance() >= 0)
                        0f
                    else
                        (originView.top - destinationView.top).toFloat()
                }
                BOTTOM_TO_TOP, BOTTOM_TO_RIGHT, BOTTOM_TO_LEFT, BOTTOM_TO_BOTTOM -> {
                    if (topToTopDistance() >= 0)
                        originView.height.toFloat()
                    else
                        (originView.top - destinationView.top + originView.height).toFloat()
                }
                else -> {
                    0f
                }
            }
        )
    }

    private fun applyFirstLine(path: Path) {
        path.rLineTo(
            when (preferredPath) {
                RIGHT_TO_TOP, RIGHT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_BOTTOM -> {
                    dentSize.toFloat()
                }
                LEFT_TO_TOP, LEFT_TO_RIGHT, LEFT_TO_LEFT, LEFT_TO_BOTTOM -> {
                    -dentSize.toFloat()
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                TOP_TO_TOP, TOP_TO_RIGHT, TOP_TO_LEFT, TOP_TO_BOTTOM -> {
                    -dentSize.toFloat()
                }
                BOTTOM_TO_TOP, BOTTOM_TO_RIGHT, BOTTOM_TO_LEFT, BOTTOM_TO_BOTTOM -> {
                    dentSize.toFloat()
                }
                else -> {
                    0f
                }
            }
        )
    }

    private fun applySecondLine(path: Path) {
        path.rLineTo(
            when (preferredPath) {
                TOP_TO_TOP -> {
                    if (topToTopDistance() >= 0)
                        if (horizontalMidDistance() >= 0)
                            max(horizontalMidDistance(), (originView.width / 2 + dentSize).toFloat())
                        else
                            min(horizontalMidDistance(), -(originView.width / 2 + dentSize).toFloat())
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() <= 0)
                            horizontalMidDistance() + destinationView.width / 2 + dentSize
                        else
                            horizontalMidDistance() - destinationView.width / 2 - dentSize
                }
                BOTTOM_TO_BOTTOM -> {
                    if (bottomToBottomDistance() < 0)
                        if (horizontalMidDistance() >= 0)
                            max(horizontalMidDistance(), (originView.width / 2 + dentSize).toFloat())
                        else
                            min(horizontalMidDistance(), -(originView.width / 2 + dentSize).toFloat())
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() <= 0)
                            horizontalMidDistance() + destinationView.width / 2 + dentSize
                        else
                            horizontalMidDistance() - destinationView.width / 2 - dentSize
                }
                LEFT_TO_LEFT -> {
                    if (leftToLeftDistance() >= 0)
                        0f
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            leftToLeftDistance()
                        else
                            0f
                }
                RIGHT_TO_RIGHT -> {
                    if (rightToRightDistance() < 0)
                        0f
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            rightToRightDistance()
                        else
                            0f
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                TOP_TO_TOP -> {
                    if (topToTopDistance() >= 0)
                        0f
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            topToTopDistance()
                        else
                            0f
                }
                BOTTOM_TO_BOTTOM -> {
                    if (bottomToBottomDistance() < 0)
                        0f
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            bottomToBottomDistance()
                        else
                            0f
                }
                LEFT_TO_LEFT -> {
                    if (leftToLeftDistance() >= 0)
                        if (verticalMidDistance() >= 0)
                            max(verticalMidDistance(), (originView.height / 2 + dentSize).toFloat())
                        else
                            min(verticalMidDistance(), -(originView.height / 2 + dentSize).toFloat())
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() <= 0)
                            verticalMidDistance() + destinationView.height / 2 + dentSize
                        else
                            verticalMidDistance() - destinationView.height / 2 - dentSize
                }
                RIGHT_TO_RIGHT -> {
                    if (rightToRightDistance() < 0)
                        if (verticalMidDistance() >= 0)
                            max(verticalMidDistance(), (originView.height / 2 + dentSize).toFloat())
                        else
                            min(verticalMidDistance(), -(originView.height / 2 + dentSize).toFloat())
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() <= 0)
                            verticalMidDistance() + destinationView.height / 2 + dentSize
                        else
                            verticalMidDistance() - destinationView.height / 2 - dentSize
                }
                else -> {
                    0f
                }
            }
        )
    }

    private fun applyThirdLine(path: Path) {
        path.rLineTo(
            when (preferredPath) {
                TOP_TO_TOP -> {
                    if (topToTopDistance() >= 0)
                        0f
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            horizontalMidDistance()
                        else
                            0f
                }
                BOTTOM_TO_BOTTOM -> {
                    if (bottomToBottomDistance() < 0)
                        0f
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            horizontalMidDistance()
                        else
                            0f
                }
                LEFT_TO_LEFT -> {
                    if (leftToLeftDistance() >= 0)
                        leftToLeftDistance()
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else
                            leftToLeftDistance()
                }
                RIGHT_TO_RIGHT -> {
                    if (rightToRightDistance() < 0)
                        rightToRightDistance()
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else
                            rightToRightDistance()
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                TOP_TO_TOP -> {
                    if (topToTopDistance() >= 0)
                        topToTopDistance()
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else
                            topToTopDistance()
                }
                BOTTOM_TO_BOTTOM -> {
                    if (bottomToBottomDistance() < 0)
                        bottomToBottomDistance()
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else
                            bottomToBottomDistance()
                }
                LEFT_TO_LEFT -> {
                    if (leftToLeftDistance() >= 0)
                        0f
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            verticalMidDistance()
                        else
                            0f
                }
                RIGHT_TO_RIGHT -> {
                    if (rightToRightDistance() < 0)
                        0f
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            verticalMidDistance()
                        else
                            0f
                }
                else -> {
                    0f
                }
            }
        )
    }

    private fun applyFourthLine(path: Path) {
        path.rLineTo(
            when (preferredPath) {
                TOP_TO_TOP -> {
                    if (topToTopDistance() >= 0)
                        if (abs(horizontalMidDistance()) >= originView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() < 0)
                            horizontalMidDistance() + originView.width / 2 + dentSize
                        else
                            horizontalMidDistance() - originView.width / 2 - dentSize
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() <= 0)
                            -(destinationView.width / 2 + dentSize).toFloat()
                        else
                            (destinationView.width / 2 + dentSize).toFloat()
                }
                BOTTOM_TO_BOTTOM -> {
                    if (bottomToBottomDistance() < 0)
                        if (abs(horizontalMidDistance()) >= originView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() < 0)
                            horizontalMidDistance() + originView.width / 2 + dentSize
                        else
                            horizontalMidDistance() - originView.width / 2 - dentSize
                    else
                        if (abs(horizontalMidDistance()) >= destinationView.width / 2 + dentSize)
                            0f
                        else if (horizontalMidDistance() <= 0)
                            -(destinationView.width / 2 + dentSize).toFloat()
                        else
                            (destinationView.width / 2 + dentSize).toFloat()
                }
                LEFT_TO_LEFT -> {
                    0f
                }
                RIGHT_TO_RIGHT -> {
                    0f
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                TOP_TO_TOP -> {
                    0f
                }
                BOTTOM_TO_BOTTOM -> {
                    0f
                }
                LEFT_TO_LEFT -> {
                    if (leftToLeftDistance() >= 0)
                        if (abs(verticalMidDistance()) >= originView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() < 0)
                            verticalMidDistance() + originView.height / 2 + dentSize
                        else
                            verticalMidDistance() - originView.height / 2 - dentSize
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() <= 0)
                            -(destinationView.height / 2 + dentSize).toFloat()
                        else
                            (destinationView.height / 2 + dentSize).toFloat()
                }
                RIGHT_TO_RIGHT -> {
                    if (rightToRightDistance() < 0)
                        if (abs(verticalMidDistance()) >= originView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() < 0)
                            verticalMidDistance() + originView.height / 2 + dentSize
                        else
                            verticalMidDistance() - originView.height / 2 - dentSize
                    else
                        if (abs(verticalMidDistance()) >= destinationView.height / 2 + dentSize)
                            0f
                        else if (verticalMidDistance() <= 0)
                            -(destinationView.height / 2 + dentSize).toFloat()
                        else
                            (destinationView.height / 2 + dentSize).toFloat()
                }
                else -> {
                    0f
                }
            }
        )
    }

    private fun applyFifthLine(path: Path) {
        path.rLineTo(
            when (preferredPath) {
                RIGHT_TO_TOP, RIGHT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_BOTTOM -> {
                    -dentSize.toFloat()
                }
                LEFT_TO_TOP, LEFT_TO_RIGHT, LEFT_TO_LEFT, LEFT_TO_BOTTOM -> {
                    dentSize.toFloat()
                }
                else -> {
                    0f
                }
            },
            when (preferredPath) {
                TOP_TO_TOP, TOP_TO_RIGHT, TOP_TO_LEFT, TOP_TO_BOTTOM -> {
                    dentSize.toFloat()
                }
                BOTTOM_TO_TOP, BOTTOM_TO_RIGHT, BOTTOM_TO_LEFT, BOTTOM_TO_BOTTOM -> {
                    -dentSize.toFloat()
                }
                else -> {
                    0f
                }
            }
        )
    }

//    private fun createPathTopToTop(): Path {
//        val optimalPath = Path()
//        val distTopTop = (originView.top - destinationView.top).toFloat()
//        val horizontalDist =
//            (originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2)).toFloat()
//
//
//
//        return when {
//            horizontalDist <= 0 -> {
//                optimalPath.moveTo(originView.width.toFloat() / 2, 0f)
//                optimalPath.rLineTo(0f, -dentSize.toFloat())
//                if (-horizontalDist < originView.width / 2 + dentSize)
//                    optimalPath.rLineTo(originView.width.toFloat() / 2 + dentSize, 0f)
//                else
//                    optimalPath.rLineTo(-horizontalDist, 0f)
//
//                optimalPath.rLineTo(0f, -distTopTop)
//                if (-horizontalDist < originView.width / 2 + dentSize)
//                    optimalPath.rLineTo(
//                        -originView.width.toFloat() / 2 - dentSize - horizontalDist,
//                        0f
//                    )
//                optimalPath.rLineTo(0f, dentSize.toFloat())
//                applyMarginOffset(optimalPath)
//            }
//            else -> {
//                optimalPath.moveTo(destinationView.width.toFloat() / 2 + horizontalDist, 0f)
//                optimalPath.rLineTo(0f, -dentSize.toFloat())
//                optimalPath.rLineTo(-horizontalDist, 0f)
//                optimalPath.rLineTo(0f, -distTopTop)
//                optimalPath.rLineTo(0f, dentSize.toFloat())
//                applyMarginOffset(optimalPath)
//            }
//        }
//    }

    private fun applyMarginOffset(optimalPath: Path): Path {
        optimalPath.offset(
            -(this.layoutParams as ConstraintLayout.LayoutParams).marginStart.toFloat(),
            -(this.layoutParams as ConstraintLayout.LayoutParams).topMargin.toFloat()
        )
        return optimalPath
    }

//    private fun createPathSideToSide(): Path {
//        val optimalPath = Path()
//        val distLeftRight = abs(originView.left - destinationView.right)
//        val distRightLeft = abs(originView.right - destinationView.left)
//        val verticalDist =
//            abs((originView.top + (originView.height / 2)) - (destinationView.top + (destinationView.height / 2))).toFloat()
//        val arrHorizontalDist = listOf(distLeftRight, distRightLeft)
//
//        if (isAbove())
//            when (arrHorizontalDist.min()) {
//                distLeftRight -> {
//                    optimalPath.moveTo(
//                        destinationView.width.toFloat() + distLeftRight,
//                        originView.height.toFloat() / 2
//                    )
//                    optimalPath.rLineTo(-dentSize.toFloat(), 0.0F)
//                    optimalPath.rLineTo(0.0F, verticalDist)
//                    optimalPath.lineTo(
//                        destinationView.width.toFloat(),
//                        originView.height.toFloat() / 2 + verticalDist
//                    )
//                }
//                distRightLeft -> {
//                    optimalPath.moveTo(
//                        originView.width.toFloat(),
//                        originView.height.toFloat() / 2
//                    )
//                    optimalPath.rLineTo(dentSize.toFloat(), 0.0F)
//                    optimalPath.rLineTo(0.0F, verticalDist)
//                    optimalPath.lineTo(
//                        originView.width.toFloat() + distRightLeft.toFloat(),
//                        originView.height.toFloat() / 2 + verticalDist
//                    )
//                }
//            }
//        else
//            when (arrHorizontalDist.min()) {
//                distLeftRight -> {
//                    optimalPath.moveTo(
//                        destinationView.width.toFloat() + distLeftRight,
//                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist
//                    )
//                    optimalPath.rLineTo(-dentSize.toFloat(), 0.0F)
//                    optimalPath.rLineTo(0.0F, -verticalDist)
//                    optimalPath.lineTo(
//                        destinationView.width.toFloat(),
//                        destinationView.height.toFloat() / 2
//                    )
//                }
//                distRightLeft -> {
//                    optimalPath.moveTo(
//                        originView.width.toFloat(),
//                        destinationView.height.toFloat() - destinationView.height / 2 + verticalDist
//                    )
//                    optimalPath.rLineTo(dentSize.toFloat(), 0.0F)
//                    optimalPath.rLineTo(0.0F, -verticalDist)
//                    optimalPath.lineTo(
//                        originView.width.toFloat() + distRightLeft,
//                        destinationView.height.toFloat() / 2
//                    )
//                }
//            }
//        //required because of the margins, the margins are always the size of the dent
//        return applyMarginOffset(optimalPath)
//    }

//    private fun createPathTopToBottom(): Path {
//        val optimalPath = Path()
//        val distTopBottom = abs(originView.top - destinationView.bottom)
//        val distBottomTop = abs(originView.bottom - destinationView.top)
//        val horizontalDist =
//            abs((originView.left + (originView.width / 2)) - (destinationView.left + (destinationView.width / 2))).toFloat()
//        val arrVerticalDist = listOf(distTopBottom, distBottomTop)
//
//        if (isLeft())
//            when (arrVerticalDist.min()) {
//                distTopBottom -> {
//                    optimalPath.moveTo(
//                        originView.width.toFloat() / 2,
//                        destinationView.height.toFloat() + distTopBottom
//                    )
//                    optimalPath.rLineTo(0.0F, -dentSize.toFloat())
//                    optimalPath.rLineTo(horizontalDist, 0.0F)
//                    optimalPath.lineTo(
//                        originView.width.toFloat() / 2 + horizontalDist,
//                        destinationView.height.toFloat()
//                    )
//                }
//                distBottomTop -> {
//                    optimalPath.moveTo(
//                        originView.width.toFloat() / 2,
//                        originView.height.toFloat()
//                    )
//                    optimalPath.rLineTo(0.0F, dentSize.toFloat())
//                    optimalPath.rLineTo(horizontalDist, 0.0F)
//                    optimalPath.lineTo(
//                        originView.width.toFloat() / 2 + horizontalDist,
//                        originView.height.toFloat() + distBottomTop.toFloat()
//                    )
//                }
//            }
//        else
//            when (arrVerticalDist.min()) {
//                distTopBottom -> {
//                    optimalPath.moveTo(
//                        destinationView.width.toFloat() - destinationView.width / 2 + horizontalDist,
//                        destinationView.height.toFloat() + distTopBottom
//                    )
//                    optimalPath.rLineTo(0.0F, -dentSize.toFloat())
//                    optimalPath.rLineTo(-horizontalDist, 0.0F)
//                    optimalPath.lineTo(
//                        destinationView.width.toFloat() / 2,
//                        destinationView.height.toFloat()
//                    )
//                }
//                distBottomTop -> {
//                    optimalPath.moveTo(
//                        destinationView.width.toFloat() - destinationView.width / 2 + horizontalDist,
//                        originView.height.toFloat()
//                    )
//                    optimalPath.rLineTo(0.0F, dentSize.toFloat())
//                    optimalPath.rLineTo(-horizontalDist, 0.0F)
//                    optimalPath.lineTo(
//                        destinationView.width.toFloat() / 2,
//                        originView.height.toFloat() + distBottomTop
//                    )
//                }
//            }
//        //required because of the margins, the margins are always the size of the dent
//        return applyMarginOffset(optimalPath)
//    }

//    private fun isSideToSidePossible(): Boolean {
//        return originView.right < destinationView.left || originView.left > destinationView.right
//    }
//
//    private fun isTopToBottomPossible(): Boolean {
//        return originView.bottom < destinationView.top || originView.top > destinationView.bottom
//    }

    private fun topToTopDistance() = (destinationView.top - originView.top).toFloat()

    private fun bottomToBottomDistance() = (destinationView.bottom - originView.bottom).toFloat()

    private fun leftToLeftDistance() = (destinationView.left - originView.left).toFloat()

    private fun rightToRightDistance() = (destinationView.right - originView.right).toFloat()

    private fun horizontalMidDistance() =
        ((destinationView.left + (destinationView.width / 2)) - (originView.left + (originView.width / 2))).toFloat()

    private fun verticalMidDistance() =
        ((destinationView.top + (destinationView.height / 2)) - (originView.top + (originView.height / 2))).toFloat()

    companion object PathDirection {
        const val LEFT_TO_LEFT = 0
        const val LEFT_TO_TOP = 1
        const val LEFT_TO_RIGHT = 2
        const val LEFT_TO_BOTTOM = 3
        const val TOP_TO_LEFT = 4
        const val TOP_TO_TOP = 5
        const val TOP_TO_RIGHT = 6
        const val TOP_TO_BOTTOM = 7
        const val RIGHT_TO_LEFT = 8
        const val RIGHT_TO_TOP = 9
        const val RIGHT_TO_RIGHT = 10
        const val RIGHT_TO_BOTTOM = 11
        const val BOTTOM_TO_LEFT = 12
        const val BOTTOM_TO_TOP = 13
        const val BOTTOM_TO_RIGHT = 14
        const val BOTTOM_TO_BOTTOM = 15
        const val VERTICAL = 16
        const val HORIZONTAL = 17
    }
}