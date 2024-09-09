package com.example.licenseplaterec

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class history : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.history)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        val extras = intent.extras
        var Res = extras?.getString("ResString") as String
        Log.i("INFO", Res)
        val typeToken = object : TypeToken<List<HistoryInfo>>() {}.type
        val res = Gson().fromJson<List<HistoryInfo>>(Res, typeToken)


        Log.i("INFO", res[0].kenteken.toString())
        Log.i("INFO", res[0].brandstofOmschrijving.toString())
        Log.i("INFO", res[0].canpark.toString())

        res.size
        val layout = findViewById<LinearLayout>(R.id.ExampleLayout)

        for (auto in res) {
            val f1 = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                val i1 = ImageView(this@history).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        200,
                        200
                    ).apply {
                        gravity = Gravity.END
                    }


                    if (auto.canpark == true) {
                        setImageResource(R.drawable.checkmark)
                    } else {
                        setImageResource(R.drawable.cross)
                    }

                }

                addView(i1)
                val l1 = LinearLayout(this@history).apply {
                    orientation = LinearLayout.VERTICAL

                    val kenteken = TextView(this@history).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )

                        textSize = 20f
                        typeface = Typeface.MONOSPACE
                        text = auto.kenteken.toString()
                    }
                    addView(kenteken)

                    val type = TextView(this@history).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )

                        textSize = 20f
                        typeface = Typeface.MONOSPACE
                        text = auto.brandstofOmschrijving.toString()
                    }
                    addView(type, 1)
                }
                addView(l1)
            }
            layout.addView(f1, 1)

        }
    }
}