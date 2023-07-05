package br.com.devsrsouza.svg2compose

import java.io.File

fun main() {
    val iconTest = File("src/test/assets")
    val src = File("build/generated-icons").apply { mkdirs() }

    val localsGroup = "IntelliJTheme.iconColors"
    val intellijColorLocals = listOf(
        Pair("FF6C707E", "$localsGroup.generalStroke"),
        Pair("FFEBECF0", "$localsGroup.generalFill"),
        Pair("FF4682FA", "$localsGroup.blueSolid"),
    )

    Svg2Compose.parse(
        applicationIconPackage = "org.jetbrains.jewel.icons",
        accessorName = "AllIcons",
        outputSourceDirectory = src,
        vectorsDirectory = iconTest,
        compositionLocals = intellijColorLocals,
        fileFilter = { !it.nameWithoutExtension.contains("_dark") },
        iconNameTransformer = { name, _ ->
            name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        },
        type = VectorType.SVG
    )
}