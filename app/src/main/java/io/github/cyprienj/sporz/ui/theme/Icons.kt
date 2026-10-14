package io.github.cyprienj.sporz.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Stroke icons of design.html (24 × 24 viewport, round caps). SVG circles and
 * rects are converted to path data. Tint them with `Icon(tint = …)`.
 */
object SporzIcons {
    val Plus by icon("Plus", 1.8f, "M12 5v14M5 12h14")
    val Close by icon("Close", 1.8f, "M6 6l12 12M18 6L6 18")
    val Check by icon("Check", 2.4f, "M5 12.5l4.5 4.5L19 7.5")
    val Back by icon("Back", 2f, "M15 5l-7 7 7 7")
    val ChevronRight by icon("ChevronRight", 2f, "M9 5l7 7-7 7")
    val ChevronDown by icon("ChevronDown", 2f, "M6 9l6 6 6-6")
    val ChevronUp by icon("ChevronUp", 2f, "M6 15l6-6 6 6")
    val Join by icon("Join", 1.8f, "M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4", "M10 8l4 4-4 4M14 12H4")
    val Info by icon("Info", 1.8f, circle(12f, 12f, 9f), "M12 11v5M12 8h.01")
    val ScanQr by icon(
        "ScanQr", 1.8f,
        "M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3",
        "M4 12h16",
    )
    val Night by icon("Night", 1.8f, "M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z")
    val Day by icon(
        "Day", 1.8f, circle(12f, 12f, 4f),
        "M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4",
    )
    val Players by icon(
        "Players", 1.8f, circle(9f, 8f, 3.5f),
        "M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6",
        "M16 4.5a3.5 3.5 0 0 1 0 7M18 14c2 .6 3.5 2.6 3.5 6",
    )
    val Journal by icon("Journal", 1.8f, "M5 4h11a3 3 0 0 1 3 3v13H8a3 3 0 0 1-3-3z", "M9 9h6M9 13h6")
    val RoleCard by icon(
        "RoleCard", 1.8f,
        "M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
        circle(12f, 10f, 3f),
        "M8.5 17c.8-1.5 2-2.2 3.5-2.2s2.7.7 3.5 2.2",
    )
    val Hide by icon(
        "Hide", 1.8f, "M3 3l18 18",
        "M10.6 6.1A9.8 9.8 0 0 1 12 6c5 0 9 6 9 6a16 16 0 0 1-3 3.4M6.5 7.5C4.3 9 3 12 3 12s4 6 9 6c1.4 0 2.7-.4 3.8-1",
    )
    val Download by icon("Download", 1.8f, "M12 4v11M7 10l5 5 5-5M5 20h14")

    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

    private fun icon(name: String, strokeWidth: Float, vararg paths: String) = lazy {
        ImageVector.Builder(
            name = "Sporz.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = strokeWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
    }
}
