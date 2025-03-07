package org.jetbrains.dokka.analysis.kotlin.descriptors.compiler.impl.moduledocs

import java.io.File

internal abstract class ModuleAndPackageDocumentationSource {
    abstract val sourceDescription: String
    abstract val documentation: String
    override fun toString(): String = sourceDescription
}

internal data class ModuleAndPackageDocumentationFile(private val file: File) : ModuleAndPackageDocumentationSource() {
    override val sourceDescription: String = file.path
    override val documentation: String by lazy(LazyThreadSafetyMode.PUBLICATION) { file.readText() }
}
