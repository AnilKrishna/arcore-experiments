package com.ustwo.arcoreexperiment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import java.io.IOException

class ImageRecognitionActivity: AppCompatActivity() {

    private val TAG = ImageRecognitionActivity::class.java.simpleName

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private lateinit var arSceneView: ArSceneView
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)
        arSceneView = findViewById(R.id.surfaceview)
        initializeSceneView()
    }


    override fun onResume() {
        super.onResume()

        session = Session(this)
        configureSession()
        arSceneView.setupSession(session)

        try {

        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available. Please restart the app.",e)
            session = null

        }

        session?.resume()
        arSceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        if (session != null)
        {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            arSceneView.pause()
            session?.pause()
        }
    }


    private fun initializeSceneView() {
        arSceneView.scene.addOnUpdateListener { this.onUpdateFrame(it) }
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arSceneView.arFrame
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in updatedAugmentedImages)
        {
            if (augmentedImage.trackingState === TrackingState.TRACKING)
            {
                // Check camera image matches our reference image
                if (augmentedImage.name == "mural")
                {
                    val node = AugmentedImageNode(this, "Mesh_Dinosaur.sfb")
                    node.setImage(augmentedImage)
                    arSceneView.scene.addChild(node)
                }
            }
        }
    }

    private fun configureSession() {
        val config = Config(session)
        if (!setupAugmentedImageDb(config))
        {
            Log.e(TAG, "Could not setup augmented image database")
        }
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session?.configure(config)
    }

    private fun setupAugmentedImageDb(config:Config):Boolean {
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        val augmentedImageBitmap = loadAugmentedImage() ?: return false
        augmentedImageDatabase.addImage("mural", augmentedImageBitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImage(): Bitmap? {
        try
        {
            assets.open("mural.png").use { `is`-> return BitmapFactory.decodeStream(`is`) }
        }
        catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e)
        }
        return null
    }

}