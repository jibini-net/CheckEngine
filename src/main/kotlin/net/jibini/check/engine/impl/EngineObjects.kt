package net.jibini.check.engine.impl

import net.jibini.check.annotation.EngineObject
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

object EngineObjects
{
    private val log = LoggerFactory.getLogger(javaClass)

    @JvmStatic
    fun placeInstance(instance: Any, placeIn: Any)
    {
        // Scan all properties to find engine object fields
        for (prop in placeIn::class.declaredMemberProperties)
            // Make sure has annotation and is right type
            if (prop.hasAnnotation<EngineObject>() && prop.returnType.classifier is KClass<*>
                && instance::class.isSubclassOf(prop.returnType.classifier as KClass<*>))
            {
                // Set to accessible in case the field is private
                prop.isAccessible = true

                // Make sure the property isn't final
                if (prop is KMutableProperty<*>)
                    // Set the property value
                    prop.setter.call(placeIn, instance)
                else
                    log.error("Could not set annotated engine object; property is final")
            }

//        for (field in placeIn::class.java.declaredFields)
//            if (field.getAnnotationsByType(EngineObject::class.java).isNotEmpty()
//                && instance::class.isSubclassOf(field.type.kotlin))
//            {
//                field.set(placeIn, instance)
//            }
    }
}