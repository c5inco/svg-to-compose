package des.c5inco.svg2compose

import androidx.compose.material.icons.generator.*
import androidx.compose.material.icons.generator.util.backingPropertySpec
import androidx.compose.material.icons.generator.util.withBackingProperty
import androidx.compose.material.icons.generator.vector.Fill
import androidx.compose.material.icons.generator.vector.Vector
import androidx.compose.material.icons.generator.vector.VectorNode
import com.squareup.kotlinpoet.*

/**
 * Generator for creating a Kotlin source file with a VectorAsset property for the given [vector],
 * with name [iconName] and composition locals [compositionLocals].
 *
 */
class ThemedVectorAssetGenerator(
    private val iconName: String,
    private val iconGroupPackage: String,
    private val vector: Vector,
    private val compositionLocals: List<Pair<String, String>>,
) {
    /**
     * @return a [FileSpec] representing a Kotlin source file containing the property for this
     * programmatic [vector] representation.
     *
     * The package name and hence file location of the generated file is:
     * [PackageNames.MaterialIconsPackage] + [IconTheme.themePackageName].
     */
    fun createFileSpec(groupClassName: ClassName): VectorAssetGenerationResult {
        // Use a unique property name for the private backing property. This is because (as of
        // Kotlin 1.4) each property with the same name will be considered as a possible candidate
        // for resolution, regardless of the access modifier, so by using unique names we reduce
        // the size from ~6000 to 1, and speed up compilation time for these icons.
        @OptIn(ExperimentalStdlibApi::class)
        val backingPropertyName = "_" + iconName.replaceFirstChar { it.lowercase() }
        val backingProperty = backingPropertySpec(name = backingPropertyName, IconsClassNames.IntellijIconData)

        val composable = FunSpec.builder(name = "${iconName}Composable")
            .addParameter("colorScheme", IconsClassNames.IntellijIconColors)
            .addModifiers(KModifier.PRIVATE)
            .returns(ClassNames.ImageVector)
            .addCode(iconBuilder(backingProperty))
            .build()

        println(iconName)
        val generation = FileSpec.builder(
            packageName = iconGroupPackage,
            fileName = iconName
        ).addProperty(
            PropertySpec.builder(name = iconName, type = IconsClassNames.IntellijIconData)
                .receiver(groupClassName)
                .getter(
                    FunSpec.getterBuilder()
                        .withBackingProperty(backingProperty) {
                            addStatement("%N = %T(imageVector = { %N(it) })", backingProperty, IconsClassNames.IntellijIconData, composable)
                        }.build()
                )
                .build()
        ).addProperty(
            backingProperty
        ).addFunction(
            composable
        ).setIndent().build()

        return VectorAssetGenerationResult(generation, iconName)
    }

    private fun iconBuilder(backingProperty: PropertySpec): CodeBlock {
        val parameterList = with(vector) {
            listOfNotNull(
                "name = \"${iconName}\"",
                "defaultWidth = ${width.withMemberIfNotNull}",
                "defaultHeight = ${height.withMemberIfNotNull}",
                "viewportWidth = ${viewportWidth}f",
                "viewportHeight = ${viewportHeight}f"
            )
        }

        val parameters = parameterList.joinToString(prefix = "(", postfix = ")")

        val members: Array<Any> = listOfNotNull(
            MemberNames.ImageVectorBuilder,
            vector.width.memberName,
            vector.height.memberName
        ).toTypedArray()

        return buildCodeBlock {
            beginControlFlow(
                "return %M$parameters.apply",
                *members
            )
            vector.nodes.forEach { node -> addRecursively(node, compositionLocals) }
            endControlFlow()
            addStatement(".build()")
        }
    }
}

/**
 * Recursively adds function calls to construct the given [vectorNode] and its children.
 */
private fun CodeBlock.Builder.addRecursively(
    vectorNode: VectorNode,
    compositionLocals: List<Pair<String, String>>,
) {
    when (vectorNode) {
        // TODO: b/147418351 - add clip-paths once they are supported
        is VectorNode.Group -> {
            beginControlFlow("%M", MemberNames.Group)
            vectorNode.paths.forEach { path ->
                addRecursively(path, compositionLocals)
            }
            endControlFlow()
        }
        is VectorNode.Path -> {
            addPath(vectorNode, compositionLocals) {
                vectorNode.nodes.forEach { pathNode ->
                    addStatement(pathNode.asFunctionCall())
                }
            }
        }
    }
}

/**
 * Adds a function call to create the given [path], with [pathBody] containing the commands for
 * the path.
 */
private fun CodeBlock.Builder.addPath(
    path: VectorNode.Path,
    compositionLocals: List<Pair<String, String>> = emptyList(),
    pathBody: CodeBlock.Builder.() -> Unit
) {
    val hasStrokeColor = path.strokeColorHex != null
    var usesCompositionLocalFill = false
    var usesCompositionLocalStroke = false

    val parameterList = with(path) {
        listOfNotNull(
            "fill = ${
                if (compositionLocals.isNotEmpty()) {
                    if (getCompositionLocalFill(path, compositionLocals) != null) {
                        usesCompositionLocalFill = true
                        getCompositionLocalFill(path, compositionLocals)
                    } else {
                        getPathFill(path)
                    }
                } else {
                    getPathFill(path)
                }
            }",
            "stroke = ${
                if(hasStrokeColor) {
                    if (compositionLocals.isNotEmpty()) {
                        if (getCompositionLocalStroke(path, compositionLocals) != null) {
                            usesCompositionLocalStroke = true
                            getCompositionLocalStroke(path, compositionLocals)
                        } else {
                            "%M(%M(0x$strokeColorHex))"
                        }
                    } else {
                        "%M(%M(0x$strokeColorHex))"
                    }
                } else {
                    "null"
                }
            }",
            "fillAlpha = ${fillAlpha}f".takeIf { fillAlpha != 1f },
            "strokeAlpha = ${strokeAlpha}f".takeIf { strokeAlpha != 1f },
            "strokeLineWidth = ${strokeLineWidth.withMemberIfNotNull}",
            "strokeLineCap = %M",
            "strokeLineJoin = %M",
            "strokeLineMiter = ${strokeLineMiter}f",
            "pathFillType = %M"
        )
    }

    val parameters = parameterList.joinToString(prefix = "(", postfix = ")")

    val members: Array<Any> = listOfNotNull(
        MemberNames.Path,
        MemberNames.SolidColor.takeIf { hasStrokeColor },
        MemberNames.Color.takeIf { hasStrokeColor && !usesCompositionLocalStroke },
        path.strokeLineWidth.memberName,
        path.strokeLineCap.memberName,
        path.strokeLineJoin.memberName,
        path.fillType.memberName
    ).toMutableList().apply {
        var fillIndex = 1
        when (path.fill){
            is Fill.Color -> {
                add(fillIndex, MemberNames.SolidColor)
                if (!usesCompositionLocalFill) {
                    add(++fillIndex, MemberNames.Color)
                }
            }
            is Fill.LinearGradient -> {
                add(fillIndex, MemberNames.LinearGradient)
                path.fill.colorStops.forEach { _ ->
                    add(++fillIndex, MemberNames.Color)
                }
                add(++fillIndex, MemberNames.Offset)
                add(++fillIndex, MemberNames.Offset)
            }
            is Fill.RadialGradient -> {
                add(fillIndex, MemberNames.RadialGradient)
                path.fill.colorStops.forEach { _ ->
                    add(++fillIndex, MemberNames.Color)
                }
                add(++fillIndex, MemberNames.Offset)
            }
            null -> {}
        }
    }.toTypedArray()

    beginControlFlow(
        "%M$parameters",
        *members
    )

    pathBody()
    endControlFlow()
}

private fun getPathFill (
    path: VectorNode.Path
) = when (path.fill){
    is Fill.Color -> "%M(%M(0x${path.fill.colorHex}))"
    is Fill.LinearGradient -> {
        with (path.fill){
            "%M(" +
                    "${getGradientStops(path.fill.colorStops).toString().removeSurrounding("[","]")}, " +
                    "start = %M(${startX}f,${startY}f), " +
                    "end = %M(${endX}f,${endY}f))"
        }
    }
    is Fill.RadialGradient -> {
        with (path.fill){
            "%M(${getGradientStops(path.fill.colorStops).toString().removeSurrounding("[","]")}, " +
                    "center = %M(${centerX}f,${centerY}f), " +
                    "radius = ${gradientRadius}f)"
        }
    }
    else -> "null"
}

private fun getGradientStops(
    stops: List<Pair<Float, String>>
) = stops.map { stop ->
    "${stop.first}f to %M(0x${stop.second})"
}

private fun getCompositionLocalFill (
    path: VectorNode.Path,
    compositionLocals: List<Pair<String, String>>
) = when (path.fill){
    is Fill.Color -> {
        var localFound = false
        var newColorHex = path.fill.colorHex

        compositionLocals.forEach {
            if (!localFound) {
                if (it.first == newColorHex) {
                    newColorHex = it.second
                    localFound = true
                }
            }
        }

        if (localFound) {
            "%M(colorScheme.$newColorHex)"
        } else {
            null
        }
    }
    else -> null
}

private fun getCompositionLocalStroke (
    path: VectorNode.Path,
    compositionLocals: List<Pair<String, String>>
) = path.strokeColorHex.let { colorHex ->
    var localFound = false
    var newColorHex = colorHex

    compositionLocals.forEach {
        if (!localFound) {
            if (it.first == newColorHex) {
                newColorHex = it.second
                localFound = true
            }
        }
    }

    if (localFound) {
        "%M(colorScheme.$newColorHex)"
    } else {
        null
    }
}

private val GraphicUnit.withMemberIfNotNull: String get() = "${value}${if (memberName != null) ".%M" else "f"}"