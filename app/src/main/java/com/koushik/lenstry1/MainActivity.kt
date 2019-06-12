package com.koushik.lenstry1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer


class MainActivity : AppCompatActivity() {
    private lateinit var mDisplayText : TextView
    private lateinit var captureImage : FloatingActionButton
    private lateinit var cameraView : SurfaceView

    var displayMetrics : DisplayMetrics = DisplayMetrics()

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET)

    private val PERMISSION_REQUEST = 100

    private  lateinit var mCameraSource : CameraSource
    private lateinit var textRecognizer : TextRecognizer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var stringBuilder : StringBuilder = StringBuilder()


        windowManager.defaultDisplay.getMetrics(displayMetrics)
        var mHeight = displayMetrics.heightPixels
        var mWidth = displayMetrics.widthPixels

        textRecognizer = TextRecognizer.Builder(applicationContext).build()

        cameraView = findViewById(R.id.camera_view)
        mDisplayText = findViewById(R.id.display_text)
        captureImage = findViewById(R.id.get_image)

        //Configure the camera input
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(mHeight,mWidth)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                mCameraSource.stop()
            }

            @RequiresApi(Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    if (isPermissionGranted()) {
                        mCameraSource.start(cameraView.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext,"Error:  ${e.message}",Toast.LENGTH_SHORT).show()
                }
            }
        })



        //Setting up the text recognizer
        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems

                if (items.size() <= 0) {
                    return
                }
                mDisplayText.post {
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i)
                        stringBuilder.append(item.value)
                        stringBuilder.append("\n")
                    }

                }

            }
        })

        //On click functionality
        captureImage.setOnClickListener {
            if (!textRecognizer.isOperational) {
                Toast.makeText(applicationContext,"Dependencies not loaded yet. Please try again later.",Toast.LENGTH_SHORT).show()
            }else {
                mDisplayText.text = stringBuilder.toString()
                Handler().postDelayed({
                    mDisplayText.text = null
                    stringBuilder.clear()
                }, 4000)
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isPermissionGranted(): Boolean {
        for (permission in permissions){
            if(checkSelfPermission(permission)!= PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    private fun requestForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isPermissionGranted()) {
                mCameraSource.start(cameraView.holder)
            } else {
                Toast.makeText(applicationContext,"Permission denied",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

}
