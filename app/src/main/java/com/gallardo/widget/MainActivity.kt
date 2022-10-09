package com.gallardo.widget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private val items = listOf("LEFT_TO_LEFT", "LEFT_TO_TOP", "LEFT_TO_RIGHT", "LEFT_TO_BOTTOM",
        "TOP_TO_LEFT", "TOP_TO_TOP", "TOP_TO_RIGHT", "TOP_TO_BOTTOM", "RIGHT_TO_LEFT",
        "RIGHT_TO_TOP", "RIGHT_TO_RIGHT", "RIGHT_TO_BOTTOM", "BOTTOM_TO_LEFT", "BOTTOM_TO_TOP",
        "BOTTOM_TO_RIGHT", "BOTTOM_TO_BOTTOM", "VERTICAL", "HORIZONTAL")

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
                val connectingLine2 = findViewById<ConnectingLineView>(R.id.five_to_two)
                val connectingLine3 = findViewById<ConnectingLineView>(R.id.five_to_three)
                val connectingLine4 = findViewById<ConnectingLineView>(R.id.five_to_four)
                val connectingLine6 = findViewById<ConnectingLineView>(R.id.five_to_six)
                val connectingLine7 = findViewById<ConnectingLineView>(R.id.five_to_seven)
                val connectingLine8 = findViewById<ConnectingLineView>(R.id.five_to_eight)
                val connectingLine9 = findViewById<ConnectingLineView>(R.id.five_to_nine)

                for (i in 0..17) {
                    if(s.toString() == items[i]) {
                        connectingLine1.preferredPath = i
                        connectingLine1.preferredPath = i
                        connectingLine2.preferredPath = i
                        connectingLine3.preferredPath = i
                        connectingLine4.preferredPath = i
                        connectingLine6.preferredPath = i
                        connectingLine7.preferredPath = i
                        connectingLine8.preferredPath = i
                        connectingLine9.preferredPath = i
                    }
                }
            }
        })
    }
}