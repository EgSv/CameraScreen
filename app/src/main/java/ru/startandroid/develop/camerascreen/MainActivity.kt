package ru.startandroid.develop.camerascreen

import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Camera.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var sv: SurfaceView? = null
    private var holder: SurfaceHolder? = null
    private var holderCallback: HolderCallback? = null
    var camera: android.hardware.Camera? = null

    val CAMERA_ID = 0
    private val FULL_SCREEN = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        sv = findViewById<View>(R.id.surfaceView) as SurfaceView
        holder = sv!!.holder
        holder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        holderCallback = HolderCallback()
        holder!!.addCallback(holderCallback)
    }

    override fun onResume() {
        super.onResume()
        camera = open(CAMERA_ID)
        setPreviewSize(FULL_SCREEN)
    }

    override fun onPause() {
        super.onPause()
        if (camera != null) camera!!.release()
        camera = null
    }

    internal inner class HolderCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(holder)
                camera!!.startPreview()
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            camera!!.stopPreview()
            setCameraDisplayOrientation(CAMERA_ID)
            try {
                camera!!.setPreviewDisplay(holder)
                camera!!.startPreview()
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }
    }

    private fun setPreviewSize(fullScreen: Boolean) {
        val display: Display = windowManager.defaultDisplay
        val widthIsMax: Boolean = display.width > display.height

        val size: android.hardware.Camera.Size? = camera!!.parameters.previewSize

        val rectDisplay = RectF()
        val rectPreview = RectF()

        rectDisplay.set(0F, 0F, display.width.toFloat(), display.height.toFloat())

        if (widthIsMax) {
            rectPreview.set(0F, 0F, size!!.width.toFloat(), size.height.toFloat())
        } else {
            rectPreview.set(0F, 0F, size!!.width.toFloat(), size.height.toFloat())
        }

        val matrix = Matrix()
        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay,
                Matrix.ScaleToFit.START)
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START)
            matrix.invert(matrix)
        }

        matrix.mapRect(rectPreview)

        sv!!.layoutParams.height = rectPreview.bottom.toInt()
        sv!!.layoutParams.width = rectPreview.right.toInt()
    }

    fun setCameraDisplayOrientation(cameraId: Int) {
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when(rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result = 0

        val info = CameraInfo()
        getCameraInfo(cameraId, info)

        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation)
        } else if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = ((360 - degrees) - info.orientation)
            result += 360
        }
        result %= 360
        camera!!.setDisplayOrientation(result)
    }
}