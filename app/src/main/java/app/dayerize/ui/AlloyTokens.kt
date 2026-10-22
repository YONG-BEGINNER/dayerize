package app.dayerize.ui

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AlloyTokens {
    val Background=Color(0xFF171A1C)
    val Surface=Color(0xFF202528)
    val Outline=Color(0xFF596066)
    val Amber=Color(0xFFFFAA3C)
    val PageMargin=16.dp
    val ContentSpacing=12.dp
}
val AlloyPanelShape=CutCornerShape(8.dp)

// Layout spacing only: never overrides Android's font scale or touch-target minimums.
data class LayoutDensity(val panelPadding:Int,val gap:Int,val artworkHeight:Int)
val LocalLayoutDensity=androidx.compose.runtime.staticCompositionLocalOf { layoutDensity("comfortable") }
fun layoutDensity(key:String?)=when(key){
    "compact"->LayoutDensity(10,8,112)
    "large"->LayoutDensity(20,16,176)
    else->LayoutDensity(14,12,144)
}
