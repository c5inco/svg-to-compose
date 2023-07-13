package des.c5inco.svg2compose

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object IconsClassNames {
    val ColorScheme = ClassName("androidx.compose.material3", "ColorScheme")
    val IntellijIconData = ClassName("org.jetbrains.jewel.icons", "IntellijIconData")
    val IntellijIconColors = ClassName("org.jetbrains.jewel", "IntelliJIconColors")
    val Composable = ClassName("androidx.compose.runtime", "Composable")
    val DpSize = ClassName("androidx.compose.ui.unit", "DpSize")
    val Dp = MemberName("androidx.compose.ui.unit", "dp")
}