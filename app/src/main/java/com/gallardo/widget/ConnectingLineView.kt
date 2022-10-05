package com.gallardo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.WebStorage
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop


class ConnectingLineView  (context : Context,
                          attrs : AttributeSet
) : View(context, attrs) {

    private val idOriginView : Int = with(attrs) {
        for (iterator in 0..attributeCount)
        {
            return@with if (attrs.getAttributeNameResource(iterator) == R.attr.originView)
                attrs.getAttributeResourceValue(iterator, 0)
            else
                continue
        }
        return@with 0
    }

    private val idDestinationView : Int = with(attrs) {
        for (iterator in 0..attributeCount)
        {
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
        Log.e("DIEGO", "chamando uma vez")
        set.applyTo(constraintLayout)
        canvas.drawColor(Color.RED)
        canvas.drawLine(
            originView.right.toFloat()-originView.marginLeft,
            originView.bottom.toFloat()-originView.marginTop,
            destinationView.left.toFloat()-destinationView.marginRight,
            destinationView.top.toFloat()-destinationView.marginBottom,
            paint)
    }

    private fun adjustConstraintSet(originView: View, destinationView: View)
    {
        set.connect(id, ConstraintSet.LEFT,
            if (originView.left < destinationView.left) idOriginView else idDestinationView, ConstraintSet.LEFT, 0)
        set.connect(id, ConstraintSet.TOP,
            if (originView.top < destinationView.top) idOriginView else idDestinationView, ConstraintSet.TOP, 0)
        set.connect(id, ConstraintSet.RIGHT,
            if (destinationView.right > originView.right) idDestinationView else idOriginView, ConstraintSet.RIGHT, 0)
        set.connect(id, ConstraintSet.BOTTOM,
            if (destinationView.bottom > originView.bottom) idDestinationView else idOriginView, ConstraintSet.BOTTOM, 0)
    }

}