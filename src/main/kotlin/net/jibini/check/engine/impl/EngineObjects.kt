package net.jibini.check.engine.impl

import net.jibini.check.engine.EngineObject
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
        for (prop in placeIn::class.declaredMemberProperties)
            if (prop.hasAnnotation<EngineObject>() && prop.returnType.classifier is KClass<*>
                && instance::class.isSubclassOf(prop.returnType.classifier as KClass<*>))
            {
                prop.isAccessible = true

                if (prop is KMutableProperty<*>)
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