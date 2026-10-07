package com.example.fitness.feature.profile

import com.example.fitness.NormalizedCropRect

/**
 * Pure Kotlin mathematical calculator for positioning, scaling, and normalizing
 * 1:1 square crop rectangles for profile photos.
 *
 * Ensures:
 * 1. Image always covers the square crop frame at base zoom (1.0x).
 * 2. Original image aspect ratio is strictly preserved (never distorted or stretched).
 * 3. Pan offsets are constrained to prevent empty gaps inside the crop frame.
 * 4. Output [NormalizedCropRect] coordinates are strictly within [0.0f, 1.0f].
 */
object ProfilePhotoCropCalculator {

    /**
     * Calculates the base dimensions of the image when scaled to cover a square [viewportSize].
     */
    fun computeBaseDimensions(
        imageWidth: Int,
        imageHeight: Int,
        viewportSize: Float
    ): Pair<Float, Float> {
        if (imageWidth <= 0 || imageHeight <= 0 || viewportSize <= 0f) {
            return Pair(viewportSize, viewportSize)
        }
        val ar = imageWidth.toFloat() / imageHeight.toFloat()
        return if (ar >= 1f) {
            // Landscape or square: height covers viewport, width expands
            Pair(viewportSize * ar, viewportSize)
        } else {
            // Portrait: width covers viewport, height expands
            Pair(viewportSize, viewportSize / ar)
        }
    }

    /**
     * Computes the maximum allowed pan offsets (X, Y) from center so the image
     * completely covers the square viewport.
     */
    fun computeMaxPanOffsets(
        imageWidth: Int,
        imageHeight: Int,
        viewportSize: Float,
        zoom: Float
    ): Pair<Float, Float> {
        val (baseW, baseH) = computeBaseDimensions(imageWidth, imageHeight, viewportSize)
        val effectiveZoom = zoom.coerceIn(1.0f, 5.0f)
        val currentW = baseW * effectiveZoom
        val currentH = baseH * effectiveZoom
        val maxX = ((currentW - viewportSize) / 2f).coerceAtLeast(0f)
        val maxY = ((currentH - viewportSize) / 2f).coerceAtLeast(0f)
        return Pair(maxX, maxY)
    }

    /**
     * Computes the normalized crop rectangle (all fields in 0.0f .. 1.0f)
     * corresponding to the current viewport, zoom, and translation offsets.
     */
    fun computeNormalizedCropRect(
        imageWidth: Int,
        imageHeight: Int,
        viewportSize: Float,
        zoom: Float,
        panOffsetX: Float,
        panOffsetY: Float
    ): NormalizedCropRect {
        if (imageWidth <= 0 || imageHeight <= 0 || viewportSize <= 0f) {
            return NormalizedCropRect(0f, 0f, 1f, 1f)
        }

        val (baseW, baseH) = computeBaseDimensions(imageWidth, imageHeight, viewportSize)
        val effectiveZoom = zoom.coerceIn(1.0f, 5.0f)
        val currentW = baseW * effectiveZoom
        val currentH = baseH * effectiveZoom

        val (maxOffsetX, maxOffsetY) = computeMaxPanOffsets(imageWidth, imageHeight, viewportSize, zoom)
        val clampedOffsetX = panOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
        val clampedOffsetY = panOffsetY.coerceIn(-maxOffsetY, maxOffsetY)

        // Viewport top-left relative to image top-left
        val viewLeft = ((currentW - viewportSize) / 2f) - clampedOffsetX
        val viewTop = ((currentH - viewportSize) / 2f) - clampedOffsetY

        val normLeft = (viewLeft / currentW).coerceIn(0f, 1f)
        val normTop = (viewTop / currentH).coerceIn(0f, 1f)
        val normWidth = (viewportSize / currentW).coerceIn(0f, 1f - normLeft)
        val normHeight = (viewportSize / currentH).coerceIn(0f, 1f - normTop)

        return NormalizedCropRect(
            left = normLeft,
            top = normTop,
            width = normWidth,
            height = normHeight
        )
    }
}
