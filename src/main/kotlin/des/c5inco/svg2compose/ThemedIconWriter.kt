package des.c5inco.svg2compose

import androidx.compose.material.icons.generator.Icon
import androidx.compose.material.icons.generator.IconParser
import androidx.compose.material.icons.generator.VectorAssetGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import java.io.File

/**
 * Generates programmatic representation of all [icons] using [VectorAssetGenerator].
 *
 * @property icons the list of [Icon]s to generate Kotlin files for
 */
class ThemedIconWriter(
    private val icons: Collection<Icon>,
    private val groupClass: ClassName,
    private val groupPackage: String,
) {
    /**
     * Generates icons and writes them to [outputSrcDirectory], using [iconNamePredicate] to
     * filter what icons to generate for.
     *
     * @param outputSrcDirectory the directory to generate source files in
     * @param iconNamePredicate the predicate that filters what icons should be generated. If
     * false, the icon will not be parsed and generated in [outputSrcDirectory].
     *
     * @return MemberName of the created icons
     */
    fun generateTo(
        outputSrcDirectory: File,
        compositionLocals: List<Pair<String, String>> = emptyList(),
        iconNamePredicate: (String) -> Boolean
    ): List<MemberName> {

        return icons.filter { icon ->
            val iconName = icon.kotlinName

            iconNamePredicate(iconName)
        }.map { icon ->
            val iconName = icon.kotlinName

            val vector = IconParser(icon).parse()

            val (fileSpec, accessProperty) = ThemedVectorAssetGenerator(
                iconName,
                groupPackage,
                vector,
                compositionLocals
            ).createFileSpec(groupClass)

            fileSpec.writeTo(outputSrcDirectory)

            MemberName(fileSpec.packageName, accessProperty)
        }
    }
}