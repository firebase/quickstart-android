package com.google.firebase.samples.apps.mlkit.kotlin

/** Describing a frame info. */
data class FrameMetadata(
    private val width: Int,
    private val height: Int,
    private val rotation: Int,
    private val cameraFacing: Int
) {
    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun getRotation(): Int {
        return rotation
    }

    fun getCameraFacing(): Int {
        return cameraFacing
    }

    /** Builder of [FrameMetadata].  */
    class Builder {

        private var width: Int = 0
        private var height: Int = 0
        private var rotation: Int = 0
        private var cameraFacing: Int = 0

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): Builder {
            this.rotation = rotation
            return this
        }

        fun setCameraFacing(facing: Int): Builder {
            cameraFacing = facing
            return this
        }

        fun build(): FrameMetadata {
            return FrameMetadata(width, height, rotation, cameraFacing)
        }
    }
}