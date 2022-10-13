package com.gallardo.widget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private val items = listOf("LEFT_TO_LEFT", "LEFT_TO_TOP", "LEFT_TO_RIGHT", "LEFT_TO_BOTTOM",
        "TOP_TO_LEFT", "TOP_TO_TOP", "TOP_TO_RIGHT", "TOP_TO_BOTTOM", "RIGHT_TO_LEFT",
        "RIGHT_TO_TOP", "RIGHT_TO_RIGHT", "RIGHT_TO_BOTTOM", "BOTTOM_TO_LEFT", "BOTTOM_TO_TOP",
        "BOTTOM_TO_RIGHT", "BOTTOM_TO_BOTTOM", "VERTICAL", "HORIZONTAL", "SHORTEST")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val options = findViewById<TextInputLayout>(R.id.menu)
        val adapter = ArrayAdapter(this, R.layout.path, items)
        (options.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        (options.editText as? AutoCompleteTextView)?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val connectingLine1 = findViewById<ConnectingLineView>(R.id.five_to_one)

                for (i in 0..18) {
                    if(s.toString() == items[i]) {
                        connectingLine1.preferredPath = i
                        connectingLine1.invalidate()
                    }
                }
                findViewById<ConstraintLayout>(R.id.cl).invalidate()
            }
        })
        val dest = findViewById<View>(R.id.one)
        dest.setOnTouchListener { v, event ->

            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    val constraint1 = ConstraintSet()
                    constraint1.clone(findViewById<ConstraintLayout>(R.id.cl))
                    constraint1.setMargin(R.id.one, ConstraintLayout.LayoutParams.START,
                        event.rawX.toInt() - 65
                    )
                    constraint1.setMargin(R.id.one, ConstraintLayout.LayoutParams.TOP,
                        event.rawY.toInt() - 340
                    )
                    constraint1.applyTo(findViewById(R.id.cl))
                }
                else -> {
                    return@setOnTouchListener false
                }
            }
            v.performClick()
            true
        }
    }
}