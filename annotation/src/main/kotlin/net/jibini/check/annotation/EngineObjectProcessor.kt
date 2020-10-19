package net.jibini.check.annotation

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

@SupportedAnnotationTypes("net.jibini.check.annotation.EngineObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class EngineObjectProcessor : AbstractProcessor()
{
    private var alreadyDone = false

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(EngineObject::class.java.name)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean
    {
        if (alreadyDone)
            return false
        alreadyDone = true

        val annotated = roundEnv.getElementsAnnotatedWith(EngineObject::class.java)
        val types = ElementFilter.typesIn(annotated)

        val fileObject = processingEnv.filer.createSourceFile("net.jibini.check.generated.EngineObjects")
        val writer = fileObject.openWriter()

        writer.write("""
            package net.jibini.check.generated;
            
            public class EngineObjects
            {
                public static void initialize()
                {
                    
                }
            }
        """.trimIndent())

        writer.close()

        return false
    }
}