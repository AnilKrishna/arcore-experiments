package com.ustwo.arcoreexperiment

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class AugmentedImageNode(context: Context, filename:String) : AnchorNode() {

    private val TAG = "AugmentedImageNode"
    private lateinit var image: AugmentedImage
    // Upon construction, start loading the modelMural
    private var modelMural: CompletableFuture<ModelRenderable> = ModelRenderable.builder().setRegistryId("modelMural")
        .setSource(context, Uri.parse(filename))
        .build()

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image.
     *
     * @param image captured by your camera
     */
    fun setImage(image:AugmentedImage) {
        this.image = image
        if (!modelMural.isDone)
        {
            CompletableFuture.allOf(modelMural).thenAccept { setImage(image) }.exceptionally { throwable->
                Log.e(TAG, "Exception loading", throwable)
                null }
        }
        anchor = image.createAnchor(image.centerPose)
        val node = Node()
        //val pose = Pose.makeTranslation(0.0f,0.0f,0.0f)
        node.setParent(this)
        node.localPosition = Vector3(0.0f,0.0f,0.10f)
        node.localRotation = Quaternion.axisAngle(Vector3(-90.0f,0.0f,0.0f),1.0f)


        node.renderable = modelMural.getNow(null)
    }

    fun getImage():AugmentedImage {
        return image
    }
}