package com.example.layout

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    @Preview
    private fun Content() {
        var page by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(24.dp)
                    .border(3.dp, Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        FrameLayout(context).also {
                            it.updateScreen(page)
                        }
                    },
                    update = { view ->
                        view.updateScreen(page)
                    }
                )
            }
            Row(modifier = Modifier.wrapContentHeight()) {
                Button(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 12.dp, bottom = 24.dp)
                        .weight(1f),
                    onClick = { page-- },
                    enabled = page > 0
                ) {
                    Text(text = "Previous")
                }
                Button(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 24.dp, bottom = 24.dp)
                        .weight(1f),
                    onClick = { page++ },
                    enabled = page < PAGES_COUNT - 1
                ) {
                    Text(text = "Next")
                }
            }
        }
    }

    private fun FrameLayout.updateScreen(page: Int) {
        val res = when (page) {
            0 -> R.layout.fragment1
            1 -> R.layout.fragment2
            2 -> R.layout.fragment3
            3 -> R.layout.fragment4
            4 -> R.layout.fragment5
            else -> R.layout.fragment6
        }
        val newView = LayoutInflater.from(context).inflate(res, null, false)
        removeAllViews()
        addView(newView)
    }

    companion object {
        private const val PAGES_COUNT = 6
    }
}