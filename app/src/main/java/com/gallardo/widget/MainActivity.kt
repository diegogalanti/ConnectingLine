package com.gallardo.widget

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val newLine = ConnectingLineView(this, R.id.one, R.id.five)
//        val constraint = findViewById<ConstraintLayout>(R.id.cl)
//
//        constraint.addView(newLine)
//        newLine.setOriginView(R.id.six)
//        newLine.setDestinationView(R.id.five)
//        newLine.paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            style = Paint.Style.FILL
//            strokeWidth = 10f
//            color = Color.YELLOW
//        }
//        newLine.preferredPath = ConnectingLineView.TOP_TO_BOTTOM
    }
}