package fr.ethanmanchon.superdim

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var isOverlayActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        findViewById<SeekBar>(R.id.seekBarOpacity).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateOverlayOpacity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<View>(R.id.buttonToggle).setOnClickListener {
            if (isOverlayActive) {
                removeOverlay()
            } else {
                if (Settings.canDrawOverlays(this)) {
                    createOverlay()
                } else {
                    requestOverlayPermission()
                }
            }
        }
    }

    private fun createOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(150, 0, 0, 0))
        }

        windowManager?.addView(overlayView, params)
        isOverlayActive = true
        Toast.makeText(this, "Overlay activé", Toast.LENGTH_SHORT).show()
    }

    private fun removeOverlay() {
        windowManager?.removeView(overlayView)
        isOverlayActive = false
        Toast.makeText(this, "Overlay désactivé", Toast.LENGTH_SHORT).show()
    }

    private fun updateOverlayOpacity(opacity: Int) {
        overlayView?.setBackgroundColor(Color.argb(opacity, 0, 0, 0))
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (Settings.canDrawOverlays(this)) {
                createOverlay()
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}