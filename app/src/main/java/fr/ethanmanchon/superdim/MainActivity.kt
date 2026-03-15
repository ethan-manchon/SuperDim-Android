package fr.ethanmanchon.superdim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<SeekBar>(R.id.seekBarOpacity).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isServiceRunning) {
                    updateOverlayOpacity(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<android.view.View>(R.id.buttonToggle).setOnClickListener {
            if (isServiceRunning) {
                stopOverlayService()
            } else {
                if (Settings.canDrawOverlays(this)) {
                    startOverlayService()
                } else {
                    requestOverlayPermission()
                }
            }
        }
    }

    private fun startOverlayService() {
        val opacity = findViewById<SeekBar>(R.id.seekBarOpacity).progress
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("opacity", opacity)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
        Toast.makeText(this, "Overlay activé", Toast.LENGTH_SHORT).show()
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        isServiceRunning = false
        Toast.makeText(this, "Overlay désactivé", Toast.LENGTH_SHORT).show()
    }

    private fun updateOverlayOpacity(opacity: Int) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("opacity", opacity)
        }
        stopService(Intent(this, OverlayService::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (Settings.canDrawOverlays(this)) {
                startOverlayService()
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}