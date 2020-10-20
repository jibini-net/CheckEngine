package net.jibini.check.annotation

import java.io.PrintWriter
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.StandardLocation

@SupportedAnnotationTypes("net.jibini.check.annotation.EngineObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class EngineObjectProcessor : AbstractProcessor()
{
    private var writer: PrintWriter? = null
    private val writtenClasses = mutableListOf<String>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(EngineObject::class.java.name)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean
    {
        val annotated = roundEnv.getElementsAnnotatedWith(EngineObject::class.java)

        if (writer == null)
        {
            val probe = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT,
                "", "probe.tmp")

            val file = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                when
                {
                    probe.toUri().path.contains("kapt3/classes/test") -> "engine_scan_test.txt"

                    else -> "engine_scan.txt"
                }
            )

            probe.delete()

            writer = PrintWriter(file.openWriter())
        }

        val names = mutableListOf<String>()

        for (item in annotated)
            when
            {
                item.kind.isField -> names += (item.enclosingElement as TypeElement).qualifiedName.toString()

                item.kind.isClass -> names += (item as TypeElement).qualifiedName.toString()
            }

        for (name in names)
            if (!writtenClasses.contains(name))
            {
                writer!!.println(name)

                writtenClasses += name
            }

        writer!!.flush()

        return false
    }
}