package com.rich_beluga.richnote.ui.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class MaterialShape(
    pathData: String,
    private val referenceSize: Float = ShapesDefaults.ReferenceGrid
) : Shape {

    private val rawPath: Path by lazy { PathParser().parsePathString(pathData).toPath() }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply { addPath(rawPath) }
        val matrix = Matrix().apply {
            scale(x = size.width / referenceSize, y = size.height / referenceSize)
        }
        path.transform(matrix)
        return Outline.Generic(path)
    }

    object ShapesDefaults {
        const val ReferenceGrid = 380f
    }
}
