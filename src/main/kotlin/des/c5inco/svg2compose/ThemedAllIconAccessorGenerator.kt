package des.c5inco.svg2compose

import androidx.compose.material.icons.generator.AllIconAccessorGenerator
import androidx.compose.material.icons.generator.util.backingPropertySpec
import androidx.compose.material.icons.generator.util.withBackingProperty
import br.com.devsrsouza.svg2compose.GeneratedGroup
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import des.c5inco.svg2compose.IconsClassNames

class ThemedAllIconAccessorGenerator(
    private val iconProperties: Collection<MemberName>,
    private val accessClass: ClassName,
    private val allAssetsPropertyName: String,
    private val childGroups: List<GeneratedGroup>
): AllIconAccessorGenerator(
    iconProperties,
    accessClass,
    allAssetsPropertyName,
    childGroups
) {
    override fun createPropertySpec(
        fileSpec: FileSpec.Builder,
    ): List<PropertySpec> {
        val list = (List::class).asClassName()
        // preventing that a asset has the name List and conflict with Kotlin List import
        fileSpec.addAliasedImport(list, "____KtList")

        val allIconsType = list.parameterizedBy(IconsClassNames.IntellijIconData)
        val allIconsBackingProperty = backingPropertySpec("__$allAssetsPropertyName", allIconsType)

        // preventing import conflict when different groups has the same asset name.
        iconProperties.forEach { memberName ->
            if(memberName.simpleName == accessClass.simpleName) {
                // preventing import conflict when the asset name is the same name as the accessor
                fileSpec.addAliasedImport(memberName, "___${memberName.simpleName}")
            }
        }

        val allIconsParametersFromGroups = childGroups.map { "%M.${allAssetsPropertyName}" }

        // adding import to `AllAssets`
        childGroups.forEach {
            fileSpec.addImport(it.groupPackage, allAssetsPropertyName)
        }

        val allIconsParameters = iconProperties.map { "%M" }
        val parameters = allIconsParameters.joinToString(prefix = "(", postfix = ")")
        val childGroupsParameters = allIconsParametersFromGroups.joinToString(" + ")

        val allIconProperty = PropertySpec.builder(allAssetsPropertyName, allIconsType)
            .receiver(accessClass)
            .getter(FunSpec.getterBuilder().withBackingProperty(allIconsBackingProperty) {
                addAnnotation(AnnotationSpec.builder(IconsClassNames.Composable).build())
                addStatement(
                    "%N= ${if(childGroups.isNotEmpty()) "$childGroupsParameters + " else ""}listOf$parameters",
                    allIconsBackingProperty,
                    *(childGroups.map(::groupAllIconsMember) + iconProperties).toTypedArray()
                )
            }.build())
            .build()

        return listOf(
            allIconsBackingProperty, allIconProperty
        )
    }
}