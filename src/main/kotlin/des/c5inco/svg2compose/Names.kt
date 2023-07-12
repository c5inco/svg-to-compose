package des.c5inco.svg2compose

import androidx.compose.material.icons.generator.PackageNames
import androidx.compose.material.icons.generator.className
import com.squareup.kotlinpoet.ClassName

object IconsClassNames {
    val ColorScheme = ClassName("androidx.compose.material3", "ColorScheme")
    val IntellijIconData = ClassName("org.jetbrains.jewel.icons", "IntellijIconData")
    val IntellijIconColors = ClassName("org.jetbrains.jewel", "IntelliJIconColors")
    val Composable = ClassName("androidx.compose.runtime", "Composable")
}