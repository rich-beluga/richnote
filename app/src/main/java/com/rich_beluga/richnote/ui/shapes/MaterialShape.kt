package com.rich_beluga.richnote.ui.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Общий движок для всех фигур в этом пакете. Каждая фигура — это просто "сырые"
 * SVG path data (см. комментарий в соответствующем файле), а PathShape превращает
 * их в androidx Shape: сам ничего не знает про цвет/размер/анимацию, только форму.
 * Compose передаёт актуальный Size в createOutline() на каждой раскладке — цвет,
 * размер и всё остальное подставляются уже там, где Shape используется
 * (Modifier.clip(...), Surface(shape = ..., color = ...) и т.п.).
 *
 * Путь парсится один раз лениво (PathParser — тот же механизм, которым Compose
 * сам парсит <path android:pathData="..."> у vector drawable), дальше на каждый
 * createOutline() берётся копия и масштабируется под фактический size — исходник
 * не мутируется, поэтому один и тот же PathShape можно переиспользовать в разных
 * местах с разными размерами одновременно.
 */
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
