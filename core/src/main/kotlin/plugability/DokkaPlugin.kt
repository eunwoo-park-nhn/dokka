package org.jetbrains.dokka.plugability

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.utilities.DokkaLogger
import org.jetbrains.dokka.utilities.parseJson
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "All of Dokka's plugin API is in preview and it can be changed " +
            "in a backwards-incompatible manner with a best-effort migration. " +
            "By opting in, you acknowledge the risks of relying on preview API."
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class DokkaPluginApiPreview

/**
 * Acknowledgement for empty methods that inform users about [DokkaPluginApiPreview]
 * Also, it allows to not propagates the annotation in IDE by default when a user autogenerate methods.
 */
@DokkaPluginApiPreview
object PluginApiPreviewAcknowledgement

abstract class DokkaPlugin {
    private val extensionDelegates = mutableListOf<KProperty<*>>()
    private val unsafePlugins = mutableListOf<Lazy<Extension<*, *, *>>>()

    @PublishedApi
    internal var context: DokkaContext? = null

    protected val logger: DokkaLogger get() = context?.logger ?: throw IllegalStateException("No logger found")

    /**
     * @see PluginApiPreviewAcknowledgement
     */
    @OptIn(DokkaPluginApiPreview::class)
    protected abstract fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement
    protected inline fun <reified T : DokkaPlugin> plugin(): T = context?.plugin(T::class) ?: throwIllegalQuery()

    protected fun <T : Any> extensionPoint() = ReadOnlyProperty<DokkaPlugin, ExtensionPoint<T>> { thisRef, property ->
        ExtensionPoint(
            thisRef::class.qualifiedName ?: throw AssertionError("Plugin must be named class"),
            property.name
        )
    }
    protected fun <T : Any> extending(definition: ExtendingDSL.() -> Extension<T, *, *>) = ExtensionProvider(definition)

    protected class ExtensionProvider<T : Any> internal constructor(
        private val definition: ExtendingDSL.() -> Extension<T, *, *>
    ) {
        operator fun provideDelegate(thisRef: DokkaPlugin, property: KProperty<*>) = lazy {
            ExtendingDSL(
                thisRef::class.qualifiedName ?: throw AssertionError("Plugin must be named class"),
                property.name
            ).definition()
        }.also { thisRef.extensionDelegates += property }
    }

    internal fun internalInstall(ctx: DokkaContextConfiguration, configuration: DokkaConfiguration) {
        val extensionsToInstall = extensionDelegates.asSequence()
            .filterIsInstance<KProperty1<DokkaPlugin, Extension<*, *, *>>>() // should be always true
            .map { it.get(this) } + unsafePlugins.map { it.value }
        extensionsToInstall.forEach { if (configuration.(it.condition)()) ctx.installExtension(it) }
    }

    protected fun <T : Any> unsafeInstall(ext: Lazy<Extension<T, *, *>>) {
        unsafePlugins.add(ext)
    }
}

interface WithUnsafeExtensionSuppression {
    val extensionsSuppressed: List<Extension<*, *, *>>
}

interface ConfigurableBlock

inline fun <reified P : DokkaPlugin, reified E : Any> P.query(extension: P.() -> ExtensionPoint<E>): List<E> =
    context?.let { it[extension()] } ?: throwIllegalQuery()

inline fun <reified P : DokkaPlugin, reified E : Any> P.querySingle(extension: P.() -> ExtensionPoint<E>): E =
    context?.single(extension()) ?: throwIllegalQuery()

fun throwIllegalQuery(): Nothing =
    throw IllegalStateException("Querying about plugins is only possible with dokka context initialised")

inline fun <reified T : DokkaPlugin, reified R : ConfigurableBlock> configuration(context: DokkaContext): R? =
    context.configuration.pluginsConfiguration.firstOrNull { it.fqPluginName == T::class.qualifiedName }
        ?.let { configuration ->
            when (configuration.serializationFormat) {
                DokkaConfiguration.SerializationFormat.JSON -> parseJson(configuration.values)
                DokkaConfiguration.SerializationFormat.XML -> XmlMapper(JacksonXmlModule().apply {
                    setDefaultUseWrapper(
                        true
                    )
                }).readValue<R>(configuration.values)
            }
        }
