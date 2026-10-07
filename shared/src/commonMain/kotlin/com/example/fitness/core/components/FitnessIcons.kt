package com.example.fitness.core.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object FitnessIcons {
    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 3f)
                lineTo(2f, 12f)
                horizontalLineTo(5f)
                verticalLineTo(20f)
                horizontalLineTo(10f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(12f)
                horizontalLineTo(22f)
                close()
            }
        }.build()
    }

    val Explore: ImageVector by lazy {
        ImageVector.Builder(
            name = "Explore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Compass circle
                moveTo(12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                close()
                // Needle
                moveTo(16.24f, 7.76f)
                lineTo(14.12f, 14.12f)
                lineTo(7.76f, 16.24f)
                lineTo(9.88f, 9.88f)
                close()
            }
        }.build()
    }

    val Workout: ImageVector by lazy {
        ImageVector.Builder(
            name = "Workout",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Dumbbell center bar
                moveTo(6.5f, 12f)
                lineTo(17.5f, 12f)
                // Left inner weight
                moveTo(6.5f, 8f)
                lineTo(6.5f, 16f)
                // Left outer weight
                moveTo(3.5f, 9.5f)
                lineTo(3.5f, 14.5f)
                // Right inner weight
                moveTo(17.5f, 8f)
                lineTo(17.5f, 16f)
                // Right outer weight
                moveTo(20.5f, 9.5f)
                lineTo(20.5f, 14.5f)
            }
        }.build()
    }

    val Progress: ImageVector by lazy {
        ImageVector.Builder(
            name = "Progress",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Trend line upward
                moveTo(3f, 17f)
                lineTo(9f, 11f)
                lineTo(13f, 15f)
                lineTo(21f, 7f)
                // Arrow tip
                moveTo(16f, 7f)
                lineTo(21f, 7f)
                lineTo(21f, 12f)
            }
        }.build()
    }

    val Profile: ImageVector by lazy {
        ImageVector.Builder(
            name = "Profile",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Head
                moveTo(12f, 11f)
                curveTo(14.21f, 11f, 16f, 9.21f, 16f, 7f)
                curveTo(16f, 4.79f, 14.21f, 3f, 12f, 3f)
                curveTo(9.79f, 3f, 8f, 4.79f, 8f, 7f)
                curveTo(8f, 9.21f, 9.79f, 11f, 12f, 11f)
                close()
                // Shoulders
                moveTo(4f, 21f)
                curveTo(4f, 17f, 7.58f, 15f, 12f, 15f)
                curveTo(16.42f, 15f, 20f, 17f, 20f, 21f)
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 18f)
                lineTo(15f, 12f)
                lineTo(9f, 6f)
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 12f)
                lineTo(5f, 12f)
                moveTo(12f, 19f)
                lineTo(5f, 12f)
                lineTo(12f, 5f)
            }
        }.build()
    }

    val ArrowForward: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowForward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 12f)
                lineTo(19f, 12f)
                moveTo(12f, 5f)
                lineTo(19f, 12f)
                lineTo(12f, 19f)
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFA8FF3E)),
                strokeLineWidth = 2.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 12.5f)
                lineTo(9.5f, 17f)
                lineTo(19f, 7.5f)
            }
        }.build()
    }

    val CheckCircle: ImageVector by lazy {
        ImageVector.Builder(
            name = "CheckCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFA8FF3E)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                close()
                moveTo(8f, 12f)
                lineTo(11f, 15f)
                lineTo(16f, 9f)
            }
        }.build()
    }

    val Fire: ImageVector by lazy {
        ImageVector.Builder(
            name = "Fire",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFA8FF3E)),
                stroke = null
            ) {
                moveTo(13.5f, 2f)
                curveTo(13.5f, 2f, 12.5f, 5.5f, 9.5f, 7.5f)
                curveTo(7.5f, 8.8f, 6.5f, 10.9f, 6.5f, 13.5f)
                curveTo(6.5f, 17.1f, 9.4f, 20f, 13f, 20f)
                curveTo(16.6f, 20f, 19.5f, 17.1f, 19.5f, 13.5f)
                curveTo(19.5f, 9.5f, 16.5f, 6f, 13.5f, 2f)
                close()
            }
        }.build()
    }

    val Clock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Clock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                close()
                moveTo(12f, 6f)
                lineTo(12f, 12f)
                lineTo(16f, 14f)
            }
        }.build()
    }

    val Bell: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 8f)
                curveTo(18f, 4.69f, 15.31f, 2f, 12f, 2f)
                curveTo(8.69f, 2f, 6f, 4.69f, 6f, 8f)
                curveTo(6f, 15f, 3f, 17f, 3f, 17f)
                horizontalLineTo(21f)
                curveTo(21f, 17f, 18f, 15f, 18f, 8f)
                close()
                moveTo(13.73f, 21f)
                curveTo(13.28f, 21.62f, 12.68f, 22f, 12f, 22f)
                curveTo(11.32f, 22f, 10.72f, 21.62f, 10.27f, 21f)
            }
        }.build()
    }

    val Play: ImageVector by lazy {
        ImageVector.Builder(
            name = "Play",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null
            ) {
                moveTo(8f, 5f)
                verticalLineTo(19f)
                lineTo(19f, 12f)
                close()
            }
        }.build()
    }

    val Pause: ImageVector by lazy {
        ImageVector.Builder(
            name = "Pause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(6f, 19f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(6f)
                close()
                moveTo(14f, 5f)
                verticalLineTo(19f)
                horizontalLineTo(18f)
                verticalLineTo(5f)
                close()
            }
        }.build()
    }

    val Clipboard: ImageVector by lazy {
        ImageVector.Builder(
            name = "Clipboard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 4f)
                horizontalLineTo(19f)
                curveTo(20.1f, 4f, 21f, 4.9f, 21f, 6f)
                verticalLineTo(20f)
                curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
                horizontalLineTo(5f)
                curveTo(3.9f, 22f, 3f, 21.1f, 3f, 20f)
                verticalLineTo(6f)
                curveTo(3f, 4.9f, 3.9f, 4f, 5f, 4f)
                horizontalLineTo(8f)
                moveTo(9f, 2f)
                horizontalLineTo(15f)
                curveTo(15.55f, 2f, 16f, 2.45f, 16f, 3f)
                verticalLineTo(5f)
                curveTo(16f, 5.55f, 15.55f, 6f, 15f, 6f)
                horizontalLineTo(9f)
                curveTo(8.45f, 6f, 8f, 5.55f, 8f, 5f)
                verticalLineTo(3f)
                curveTo(8f, 2.45f, 8.45f, 2f, 9f, 2f)
                close()
                moveTo(8f, 11f)
                horizontalLineTo(16f)
                moveTo(8f, 15f)
                horizontalLineTo(16f)
            }
        }.build()
    }

    val Apple: ImageVector by lazy {
        ImageVector.Builder(
            name = "Apple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 20.5f)
                curveTo(11f, 21f, 9.5f, 21.5f, 7.5f, 21.5f)
                curveTo(4.5f, 21.5f, 2.5f, 18.5f, 2.5f, 14f)
                curveTo(2.5f, 9f, 6.5f, 6.5f, 9.5f, 6.5f)
                curveTo(10.8f, 6.5f, 12f, 7.2f, 12f, 7.2f)
                curveTo(12f, 7.2f, 13.2f, 6.5f, 14.5f, 6.5f)
                curveTo(17.5f, 6.5f, 21.5f, 9f, 21.5f, 14f)
                curveTo(21.5f, 18.5f, 19.5f, 21.5f, 16.5f, 21.5f)
                curveTo(14.5f, 21.5f, 13f, 21f, 12f, 20.5f)
                close()
                moveTo(12f, 6.5f)
                curveTo(12f, 4f, 13.5f, 2.5f, 15f, 2.5f)
            }
        }.build()
    }

    val Mobility: ImageVector by lazy {
        ImageVector.Builder(
            name = "Mobility",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Head
                moveTo(13.5f, 5f)
                curveTo(14.33f, 5f, 15f, 4.33f, 15f, 3.5f)
                curveTo(15f, 2.67f, 14.33f, 2f, 13.5f, 2f)
                curveTo(12.67f, 2f, 12f, 2.67f, 12f, 3.5f)
                curveTo(12f, 4.33f, 12.67f, 5f, 13.5f, 5f)
                close()
                // Body & running legs
                moveTo(7f, 13f)
                lineTo(10.5f, 10f)
                lineTo(13f, 7f)
                lineTo(15.5f, 11f)
                lineTo(18f, 12f)
                moveTo(11f, 13f)
                lineTo(8f, 22f)
                moveTo(13f, 12f)
                lineTo(16f, 16.5f)
                lineTo(20f, 21f)
            }
        }.build()
    }

    val Community: ImageVector by lazy {
        ImageVector.Builder(
            name = "Community",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Main person
                moveTo(12f, 10f)
                curveTo(13.66f, 10f, 15f, 8.66f, 15f, 7f)
                curveTo(15f, 5.34f, 13.66f, 4f, 12f, 4f)
                curveTo(10.34f, 4f, 9f, 5.34f, 9f, 7f)
                curveTo(9f, 8.66f, 10.34f, 10f, 12f, 10f)
                close()
                moveTo(6f, 19f)
                curveTo(6f, 15.69f, 8.69f, 13f, 12f, 13f)
                curveTo(15.31f, 13f, 18f, 15.69f, 18f, 19f)
                // Left person
                moveTo(6f, 7f)
                curveTo(6f, 5.9f, 5.1f, 5f, 4f, 5f)
                moveTo(2f, 18f)
                curveTo(2f, 15.5f, 3.8f, 13.5f, 6f, 13.2f)
                // Right person
                moveTo(18f, 7f)
                curveTo(18f, 5.9f, 18.9f, 5f, 20f, 5f)
                moveTo(22f, 18f)
                curveTo(22f, 15.5f, 20.2f, 13.5f, 18f, 13.2f)
            }
        }.build()
    }

    val Difficulty: ImageVector by lazy {
        ImageVector.Builder(
            name = "Difficulty",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 20f)
                lineTo(6f, 16f)
                moveTo(12f, 20f)
                lineTo(12f, 11f)
                moveTo(18f, 20f)
                lineTo(18f, 6f)
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11f, 19f)
                curveTo(15.42f, 19f, 19f, 15.42f, 19f, 11f)
                curveTo(19f, 6.58f, 15.42f, 3f, 11f, 3f)
                curveTo(6.58f, 3f, 3f, 6.58f, 3f, 11f)
                curveTo(3f, 15.42f, 6.58f, 19f, 11f, 19f)
                close()
                moveTo(21f, 21f)
                lineTo(16.65f, 16.65f)
            }
        }.build()
    }

    val Filter: ImageVector by lazy {
        ImageVector.Builder(
            name = "Filter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 6f)
                horizontalLineTo(20f)
                moveTo(7f, 12f)
                horizontalLineTo(17f)
                moveTo(10f, 18f)
                horizontalLineTo(14f)
            }
        }.build()
    }

    val Heart: ImageVector by lazy {
        HeartFilled
    }

    val HeartOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "HeartOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12f, 21.35f)
                close()
            }
        }.build()
    }

    val HeartFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "HeartFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12f, 21.35f)
                close()
            }
        }.build()
    }

    val Trophy: ImageVector by lazy {
        ImageVector.Builder(
            name = "Trophy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFFBBF24)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 4f)
                horizontalLineTo(18f)
                verticalLineTo(9f)
                curveTo(18f, 12.31f, 15.31f, 15f, 12f, 15f)
                curveTo(8.69f, 15f, 6f, 12.31f, 6f, 9f)
                verticalLineTo(4f)
                close()
                moveTo(6f, 7f)
                horizontalLineTo(3f)
                curveTo(3f, 9.76f, 4.79f, 11f, 6f, 11f)
                moveTo(18f, 7f)
                horizontalLineTo(21f)
                curveTo(21f, 9.76f, 19.21f, 11f, 18f, 11f)
                moveTo(12f, 15f)
                verticalLineTo(19f)
                moveTo(8f, 19f)
                horizontalLineTo(16f)
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
                moveTo(19.4f, 15f)
                lineTo(20.5f, 14.1f)
                curveTo(20.7f, 13.9f, 20.7f, 13.6f, 20.6f, 13.4f)
                lineTo(19.6f, 11.7f)
                curveTo(19.5f, 11.5f, 19.2f, 11.4f, 19f, 11.5f)
                lineTo(17.7f, 12f)
                curveTo(17.2f, 11.6f, 16.6f, 11.3f, 16f, 11.1f)
                lineTo(15.8f, 9.7f)
                curveTo(15.8f, 9.4f, 15.5f, 9.2f, 15.3f, 9.2f)
                horizontalLineTo(13.3f)
                curveTo(13f, 9.2f, 12.8f, 9.4f, 12.8f, 9.7f)
                lineTo(12.6f, 11.1f)
                curveTo(12f, 11.3f, 11.4f, 11.6f, 10.9f, 12f)
                lineTo(9.6f, 11.5f)
                curveTo(9.4f, 11.4f, 9.1f, 11.5f, 9f, 11.7f)
                lineTo(8f, 13.4f)
                curveTo(7.9f, 13.6f, 7.9f, 13.9f, 8.1f, 14.1f)
                lineTo(9.2f, 15f)
            }
        }.build()
    }

    val Mail: ImageVector by lazy {
        ImageVector.Builder(
            name = "Mail",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 4f)
                lineTo(20f, 4f)
                curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
                lineTo(22f, 18f)
                curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
                lineTo(4f, 20f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                lineTo(2f, 6f)
                curveTo(2f, 4.9f, 2.9f, 4f, 4f, 4f)
                close()
                moveTo(22f, 6f)
                lineTo(12f, 13f)
                lineTo(2f, 6f)
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Shackle
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 11f)
                verticalLineTo(7f)
                curveTo(7f, 4.24f, 9.24f, 2f, 12f, 2f)
                curveTo(14.76f, 2f, 17f, 4.24f, 17f, 7f)
                verticalLineTo(11f)
            }
            // Body
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 11f)
                horizontalLineTo(19f)
                curveTo(20.1f, 11f, 21f, 11.9f, 21f, 13f)
                verticalLineTo(20f)
                curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
                horizontalLineTo(5f)
                curveTo(3.9f, 22f, 3f, 21.1f, 3f, 20f)
                verticalLineTo(13f)
                curveTo(3f, 11.9f, 3.9f, 11f, 5f, 11f)
                close()
            }
            // Keyhole
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 15f)
                curveTo(11.45f, 15f, 11f, 15.45f, 11f, 16f)
                curveTo(11f, 16.38f, 11.21f, 16.71f, 11.53f, 16.88f)
                lineTo(11f, 19f)
                horizontalLineTo(13f)
                lineTo(12.47f, 16.88f)
                curveTo(12.79f, 16.71f, 13f, 16.38f, 13f, 16f)
                curveTo(13f, 15.45f, 12.55f, 15f, 12f, 15f)
                close()
            }
        }.build()
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "Person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Head
                moveTo(12f, 11f)
                curveTo(14.21f, 11f, 16f, 9.21f, 16f, 7f)
                curveTo(16f, 4.79f, 14.21f, 3f, 12f, 3f)
                curveTo(9.79f, 3f, 8f, 4.79f, 8f, 7f)
                curveTo(8f, 9.21f, 9.79f, 11f, 12f, 11f)
                close()
                // Shoulders
                moveTo(4f, 21f)
                curveTo(4f, 17.13f, 7.58f, 14f, 12f, 14f)
                curveTo(16.42f, 14f, 20f, 17.13f, 20f, 21f)
            }
        }.build()
    }

    val Visibility: ImageVector by lazy {
        ImageVector.Builder(
            name = "Visibility",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(1f, 12f)
                curveTo(3.5f, 6.5f, 7.5f, 4f, 12f, 4f)
                curveTo(16.5f, 4f, 20.5f, 6.5f, 23f, 12f)
                curveTo(20.5f, 17.5f, 16.5f, 20f, 12f, 20f)
                curveTo(7.5f, 20f, 3.5f, 17.5f, 1f, 12f)
                close()
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
            }
        }.build()
    }

    val VisibilityOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "VisibilityOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 3f)
                lineTo(21f, 21f)
                moveTo(10.58f, 10.58f)
                curveTo(10.22f, 10.95f, 10f, 11.45f, 10f, 12f)
                curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f)
                curveTo(12.55f, 14f, 13.05f, 13.78f, 13.42f, 13.42f)
                moveTo(9.88f, 5.12f)
                curveTo(10.57f, 5.04f, 11.27f, 5f, 12f, 5f)
                curveTo(16.5f, 5f, 20.5f, 7.5f, 23f, 12f)
                curveTo(21.94f, 14.33f, 20.17f, 16.27f, 18f, 17.43f)
                moveTo(6.1f, 6.1f)
                curveTo(4.06f, 7.55f, 2.37f, 9.58f, 1f, 12f)
                curveTo(3.5f, 16.5f, 7.5f, 19f, 12f, 19f)
                curveTo(13.64f, 19f, 15.21f, 18.57f, 16.63f, 17.81f)
            }
        }.build()
    }

    val Google: ImageVector by lazy {
        ImageVector.Builder(
            name = "Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Blue segment
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(23.745f, 12.27f)
                curveTo(23.745f, 11.48f, 23.675f, 10.73f, 23.545f, 10f)
                horizontalLineTo(12f)
                verticalLineTo(14.51f)
                horizontalLineTo(18.6f)
                curveTo(18.31f, 15.99f, 17.45f, 17.24f, 16.16f, 18.09f)
                verticalLineTo(21.09f)
                horizontalLineTo(20.07f)
                curveTo(22.36f, 18.98f, 23.745f, 15.89f, 23.745f, 12.27f)
                close()
            }
            // Green segment
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(12f, 24f)
                curveTo(15.24f, 24f, 17.96f, 22.92f, 19.98f, 21.09f)
                lineTo(16.07f, 18.09f)
                curveTo(14.99f, 18.81f, 13.62f, 19.25f, 12f, 19.25f)
                curveTo(8.87f, 19.25f, 6.22f, 17.14f, 5.27f, 14.29f)
                horizontalLineTo(1.23f)
                verticalLineTo(17.38f)
                curveTo(3.26f, 21.41f, 7.33f, 24f, 12f, 24f)
                close()
            }
            // Yellow segment
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(5.27f, 14.29f)
                curveTo(5.02f, 13.57f, 4.89f, 12.8f, 4.89f, 12f)
                curveTo(4.89f, 11.2f, 5.02f, 10.43f, 5.27f, 9.71f)
                verticalLineTo(6.62f)
                horizontalLineTo(1.23f)
                curveTo(0.44f, 8.24f, 0f, 10.06f, 0f, 12f)
                curveTo(0f, 13.94f, 0.44f, 15.76f, 1.23f, 17.38f)
                lineTo(5.27f, 14.29f)
                close()
            }
            // Red segment
            path(fill = SolidColor(Color(0xFFEA4335))) {
                moveTo(12f, 4.75f)
                curveTo(13.77f, 4.75f, 15.35f, 5.36f, 16.6f, 6.55f)
                lineTo(20.02f, 3.13f)
                curveTo(17.95f, 1.19f, 15.23f, 0f, 12f, 0f)
                curveTo(7.33f, 0f, 3.26f, 2.59f, 1.23f, 6.62f)
                lineTo(5.27f, 9.71f)
                curveTo(6.22f, 6.86f, 8.87f, 4.75f, 12f, 4.75f)
                close()
            }
        }.build()
    }

    val AppleLogo: ImageVector by lazy {
        ImageVector.Builder(
            name = "AppleLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(18.71f, 19.5f)
                curveTo(17.88f, 20.74f, 17f, 21.95f, 15.66f, 21.97f)
                curveTo(14.32f, 22f, 13.89f, 21.18f, 12.37f, 21.18f)
                curveTo(10.84f, 21.18f, 10.37f, 21.95f, 9.09f, 22f)
                curveTo(7.79f, 22.05f, 6.8f, 20.68f, 5.96f, 19.47f)
                curveTo(4.25f, 17f, 2.94f, 12.45f, 4.7f, 9.39f)
                curveTo(5.57f, 7.87f, 7.13f, 6.91f, 8.82f, 6.88f)
                curveTo(10.1f, 6.86f, 11.32f, 7.75f, 12.11f, 7.75f)
                curveTo(12.89f, 7.75f, 14.37f, 6.68f, 15.96f, 6.84f)
                curveTo(16.63f, 6.87f, 18.5f, 7.11f, 19.67f, 8.83f)
                curveTo(19.57f, 8.89f, 17.65f, 10.01f, 17.67f, 12.28f)
                curveTo(17.7f, 15f, 20.06f, 15.91f, 20.1f, 15.92f)
                curveTo(20.07f, 16.01f, 19.72f, 17.26f, 18.71f, 19.5f)
                close()
                // Leaf
                moveTo(14.97f, 4.88f)
                curveTo(15.63f, 4.08f, 16.09f, 2.97f, 15.97f, 1.85f)
                curveTo(15.01f, 1.89f, 13.82f, 2.5f, 13.14f, 3.29f)
                curveTo(12.54f, 3.99f, 12.02f, 5.12f, 12.16f, 6.22f)
                curveTo(13.23f, 6.31f, 14.32f, 5.68f, 14.97f, 4.88f)
                close()
            }
        }.build()
    }

    val VolumeUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "VolumeUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(3f, 9f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(4f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                close()
                moveTo(16.5f, 12f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
                verticalLineTo(16.02f)
                curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
                close()
                moveTo(14f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
                curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
                verticalLineTo(20.77f)
                curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
                curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
                close()
            }
        }.build()
    }

    val VolumeOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "VolumeOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(16.5f, 12f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
                verticalLineTo(10.18f)
                lineTo(16.45f, 12.63f)
                curveTo(16.48f, 12.43f, 16.5f, 12.21f, 16.5f, 12f)
                close()
                moveTo(19f, 12f)
                curveTo(19f, 12.82f, 18.84f, 13.61f, 18.55f, 14.33f)
                lineTo(20.03f, 15.81f)
                curveTo(20.65f, 14.67f, 21f, 13.38f, 21f, 12f)
                curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
                close()
                moveTo(4.27f, 3f)
                lineTo(3f, 4.27f)
                lineTo(7.73f, 9f)
                horizontalLineTo(3f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(13.27f)
                lineTo(16.25f, 17.52f)
                curveTo(15.58f, 18.04f, 14.83f, 18.45f, 14f, 18.7f)
                verticalLineTo(20.77f)
                curveTo(15.38f, 20.45f, 16.63f, 19.82f, 17.68f, 18.96f)
                lineTo(19.73f, 21f)
                lineTo(21f, 19.73f)
                lineTo(4.27f, 3f)
                close()
                moveTo(12f, 4f)
                lineTo(9.91f, 6.09f)
                lineTo(12f, 8.18f)
                verticalLineTo(4f)
                close()
            }
        }.build()
    }

    val Fullscreen: ImageVector by lazy {
        ImageVector.Builder(
            name = "Fullscreen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(7f, 14f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(17f)
                horizontalLineTo(7f)
                verticalLineTo(14f)
                close()
                moveTo(5f, 10f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(14f)
                verticalLineTo(19f)
                horizontalLineTo(19f)
                verticalLineTo(14f)
                horizontalLineTo(17f)
                verticalLineTo(17f)
                close()
                moveTo(14f, 5f)
                verticalLineTo(7f)
                horizontalLineTo(17f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(5f)
                horizontalLineTo(14f)
                close()
            }
        }.build()
    }

    val FullscreenExit: ImageVector by lazy {
        ImageVector.Builder(
            name = "FullscreenExit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(5f, 16f)
                horizontalLineTo(8f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(14f)
                horizontalLineTo(5f)
                verticalLineTo(16f)
                close()
                moveTo(8f, 8f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineTo(8f)
                verticalLineTo(8f)
                close()
                moveTo(14f, 19f)
                horizontalLineTo(16f)
                verticalLineTo(16f)
                horizontalLineTo(19f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(19f)
                close()
                moveTo(16f, 8f)
                verticalLineTo(5f)
                horizontalLineTo(14f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(8f)
                horizontalLineTo(16f)
                close()
            }
        }.build()
    }

    val Replay: ImageVector by lazy {
        ImageVector.Builder(
            name = "Replay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 5f)
                verticalLineTo(1f)
                lineTo(7f, 6f)
                lineTo(12f, 11f)
                verticalLineTo(7f)
                curveTo(15.31f, 7f, 18f, 9.69f, 18f, 13f)
                curveTo(18f, 16.31f, 15.31f, 19f, 12f, 19f)
                curveTo(8.69f, 19f, 6f, 16.31f, 6f, 13f)
                horizontalLineTo(4f)
                curveTo(4f, 17.42f, 7.58f, 21f, 12f, 21f)
                curveTo(16.42f, 21f, 20f, 17.42f, 20f, 13f)
                curveTo(20f, 8.58f, 16.42f, 5f, 12f, 5f)
                close()
            }
        }.build()
    }

    val Logout: ImageVector by lazy {
        ImageVector.Builder(
            name = "Logout",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFFF5252)),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 21f)
                horizontalLineTo(5f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                verticalLineTo(5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                horizontalLineTo(9f)
                moveTo(16f, 17f)
                lineTo(21f, 12f)
                lineTo(16f, 7f)
                moveTo(21f, 12f)
                lineTo(9f, 12f)
            }
        }.build()
    }

    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 5f)
                lineTo(12f, 19f)
                moveTo(5f, 12f)
                lineTo(19f, 12f)
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(6f, 18f)
                lineTo(18f, 6f)
            }
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder(
            name = "Edit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 17.25f)
                lineTo(3f, 21f)
                lineTo(6.75f, 21f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                lineTo(18.37f, 4.7f)
                lineTo(16.03f, 7.04f)
                lineTo(18.37f, 9.38f)
                close()
            }
        }.build()
    }

    val Archive: ImageVector by lazy {
        ImageVector.Builder(
            name = "Archive",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 4f)
                horizontalLineTo(21f)
                verticalLineTo(8f)
                horizontalLineTo(3f)
                close()
                moveTo(4f, 8f)
                verticalLineTo(20f)
                curveTo(4f, 20.55f, 4.45f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(19.55f, 21f, 20f, 20.55f, 20f, 20f)
                verticalLineTo(8f)
                moveTo(10f, 12f)
                horizontalLineTo(14f)
            }
        }.build()
    }

    val Restore: ImageVector by lazy {
        ImageVector.Builder(
            name = "Restore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(15.98f, 3f, 19.34f, 5.58f, 20.54f, 9.2f)
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(8.02f, 21f, 4.66f, 18.42f, 3.46f, 14.8f)
                moveTo(20.54f, 4.5f)
                verticalLineTo(9.2f)
                horizontalLineTo(15.84f)
            }
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder(
            name = "Delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                moveTo(19f, 6f)
                verticalLineTo(20f)
                curveTo(19f, 20.55f, 18.55f, 21f, 18f, 21f)
                horizontalLineTo(6f)
                curveTo(5.45f, 21f, 5f, 20.55f, 5f, 20f)
                verticalLineTo(6f)
                moveTo(8f, 6f)
                verticalLineTo(4f)
                curveTo(8f, 3.45f, 8.45f, 3f, 9f, 3f)
                horizontalLineTo(15f)
                curveTo(15.55f, 3f, 16f, 3.45f, 16f, 4f)
                verticalLineTo(6f)
                moveTo(10f, 11f)
                verticalLineTo(17f)
                moveTo(14f, 11f)
                verticalLineTo(17f)
            }
        }.build()
    }

    val ArrowUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 19f)
                lineTo(12f, 5f)
                moveTo(5f, 12f)
                lineTo(12f, 5f)
                lineTo(19f, 12f)
            }
        }.build()
    }

    val ArrowDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 5f)
                lineTo(12f, 19f)
                moveTo(5f, 12f)
                lineTo(12f, 19f)
                lineTo(19f, 12f)
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                close()
                moveTo(12f, 8f)
                lineTo(12f, 8.5f)
                moveTo(12f, 12f)
                lineTo(12f, 17f)
            }
        }.build()
    }

    val Camera: ImageVector by lazy {
        ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23f, 19f)
                curveTo(23f, 20.1f, 22.1f, 21f, 21f, 21f)
                horizontalLineTo(3f)
                curveTo(1.9f, 21f, 1f, 20.1f, 1f, 19f)
                verticalLineTo(8f)
                curveTo(1f, 6.9f, 1.9f, 6f, 3f, 6f)
                horizontalLineTo(7f)
                lineTo(9f, 3f)
                horizontalLineTo(15f)
                lineTo(17f, 6f)
                horizontalLineTo(21f)
                curveTo(22.1f, 6f, 23f, 6.9f, 23f, 8f)
                close()
                moveTo(12f, 17f)
                curveTo(14.21f, 17f, 16f, 15.21f, 16f, 13f)
                curveTo(16f, 10.79f, 14.21f, 9f, 12f, 9f)
                curveTo(9.79f, 9f, 8f, 10.79f, 8f, 13f)
                curveTo(8f, 15.21f, 9.79f, 17f, 12f, 17f)
                close()
            }
        }.build()
    }

    val ArmFlex: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArmFlex",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 19f)
                lineTo(6f, 13f)
                curveTo(6f, 12f, 7f, 9.5f, 10f, 6.5f)
                curveTo(11f, 5.5f, 12.5f, 5.5f, 13.5f, 6f)
                curveTo(14.5f, 6.5f, 14.5f, 8f, 14.5f, 9.5f)
                lineTo(14.5f, 10.5f)
                lineTo(11.5f, 10.5f)
                curveTo(11f, 10.5f, 11f, 11f, 11f, 11.5f)
                lineTo(11f, 14f)
                lineTo(17f, 14f)
                curveTo(18f, 14f, 18.5f, 15f, 18.5f, 16f)
                lineTo(18.5f, 18f)
                curveTo(18.5f, 19f, 17.5f, 19.5f, 16.5f, 19.5f)
                close()
            }
        }.build()
    }

    val BarChart: ImageVector by lazy {
        ImageVector.Builder(
            name = "BarChart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 19f)
                lineTo(6f, 15f)
                moveTo(11.5f, 19f)
                lineTo(11.5f, 8f)
                moveTo(17f, 19f)
                lineTo(17f, 11f)
            }
        }.build()
    }

    val FlameOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "FlameOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                curveTo(12f, 3f, 14.5f, 6.5f, 14.5f, 9f)
                curveTo(14.5f, 10.5f, 13.5f, 11f, 13.5f, 11f)
                curveTo(15f, 11.5f, 18f, 13.5f, 18f, 16.5f)
                curveTo(18f, 19.8f, 15.3f, 22f, 12f, 22f)
                curveTo(8.7f, 22f, 6f, 19.8f, 6f, 16.5f)
                curveTo(6f, 13.5f, 8.5f, 11f, 10f, 9f)
                curveTo(10.5f, 7.5f, 12f, 3f, 12f, 3f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 19f)
                curveTo(13.1f, 19f, 14f, 18.1f, 14f, 17f)
                curveTo(14f, 15.5f, 12f, 14f, 12f, 14f)
                curveTo(12f, 14f, 10f, 15.5f, 10f, 17f)
                curveTo(10f, 18.1f, 10.9f, 19f, 12f, 19f)
                close()
            }
        }.build()
    }

    val Target: ImageVector by lazy {
        ImageVector.Builder(
            name = "Target",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
                close()
                moveTo(12f, 16f)
                curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
                curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
                curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f)
                curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                moveTo(12f, 13f)
                curveTo(12.55f, 13f, 13f, 12.55f, 13f, 12f)
                curveTo(13f, 11.45f, 12.55f, 11f, 12f, 11f)
                curveTo(11.45f, 11f, 11f, 11.45f, 11f, 12f)
                curveTo(11f, 12.55f, 11.45f, 13f, 12f, 13f)
                close()
            }
        }.build()
    }
}

