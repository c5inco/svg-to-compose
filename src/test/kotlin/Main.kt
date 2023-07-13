package br.com.devsrsouza.svg2compose

import java.io.File

fun main() {
    val iconTest = File("src/test/assets")
    val src = File("build/generated-icons").apply { mkdirs() }

    val intellijColorLocals = listOf(
        Pair("FF6C707E", "generalStroke"),
        Pair("FFEBECF0", "generalFill"),
        Pair("FF4682FA", "blueSolid"),
        Pair("FFEDF3FF", "blueFill"),
        Pair("FFE7EFFD", "blueFill"), // Old but still in some
        Pair("FF3574F0", "blueStroke"),
        Pair("FF55A76A", "greenSolid"),
        Pair("FFF2FCF3", "greenFill"),
        Pair("FF208A3C", "greenStroke"),
        Pair("FFE55765", "redSolid"),
        Pair("FFFFF7F7", "redFill"),
        Pair("FFFFF5F5", "redFill"), // Old but still in some
        Pair("FFDB3B4B", "redStroke"),
        Pair("FFFFAF0F", "yellowSolid"),
        Pair("FFFFFAEB", "yellowFill"),
        Pair("FFC27D04", "yellowStroke"),
        Pair("FFFFF4EB", "orangeFill"),
        Pair("FFFCE9D9", "orangeFill"), // Old but still in some
        Pair("FFE56D17", "orangeStroke"),
        Pair("FFFAF5FF", "purpleFill"),
        Pair("FF834DF0", "purpleStroke"),
    )

    Svg2Compose.parse(
        applicationIconPackage = "org.jetbrains.jewel.icons",
        accessorName = "AllIcons",
        outputSourceDirectory = src,
        vectorsDirectory = iconTest,
        compositionLocals = intellijColorLocals,
        fileFilter = { !it.nameWithoutExtension.contains("_dark") },
        iconNameTransformer = { name, _ ->
            name
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .replace("@", "_")
        },
        type = VectorType.SVG
    )
}