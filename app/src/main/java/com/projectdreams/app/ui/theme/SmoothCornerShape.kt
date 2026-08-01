package com.projectdreams.app.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

/**
 * A smooth-corner (squircle) shape that uses superellipse curvature for
 * iOS/Material 3 Expressive–style continuous corner rounding.
 */
class AbsoluteSmoothCornerShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
    private val smoothness: Int = 60
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    constructor(size: Dp, smoothness: Int = 60) : this(
        CornerSize(size),
        CornerSize(size),
        CornerSize(size),
        CornerSize(size),
        smoothness
    )

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val r = min(topStart, min(size.width, size.height) / 2f)
        val path = smoothRoundedRectPath(size.width, size.height, r, smoothness)
        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): CornerBasedShape = AbsoluteSmoothCornerShape(topStart, topEnd, bottomEnd, bottomStart, smoothness)

    override fun toString(): String =
        "AbsoluteSmoothCornerShape(topStart=$topStart, topEnd=$topEnd, bottomEnd=$bottomEnd, bottomStart=$bottomStart, smoothness=$smoothness)"
}

/**
 * Builds a [Path] for a rectangle with continuously-curved (squircle) corners.
 */
private fun smoothRoundedRectPath(
    width: Float,
    height: Float,
    cornerRadius: Float,
    smoothness: Int
): Path {
    if (cornerRadius <= 0f || smoothness <= 0) {
        return Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f, top = 0f, right = width, bottom = height,
                    radiusX = cornerRadius.coerceAtLeast(0f),
                    radiusY = cornerRadius.coerceAtLeast(0f)
                )
            )
        }
    }

    val r = cornerRadius
    val smooth = (smoothness.coerceIn(0, 100) / 100f)
    val p = r * smooth * 1.2f

    return Path().apply {
        moveTo(0f, r + p)
        cubicTo(0f, r - r * smooth * 0.2f, r - r * smooth * 0.2f, 0f, r + p, 0f)
        lineTo(width - r - p, 0f)
        cubicTo(width - r + r * smooth * 0.2f, 0f, width, r - r * smooth * 0.2f, width, r + p)
        lineTo(width, height - r - p)
        cubicTo(
            width, height - r + r * smooth * 0.2f,
            width - r + r * smooth * 0.2f, height,
            width - r - p, height
        )
        lineTo(r + p, height)
        cubicTo(r - r * smooth * 0.2f, height, 0f, height - r + r * smooth * 0.2f, 0f, height - r - p)
        close()
    }
}
