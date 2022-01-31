package nl.adaptivity.android.kryo

import com.esotericsoftware.kryo.Registration
import com.esotericsoftware.kryo.serializers.FieldSerializer
import com.esotericsoftware.kryo.util.DefaultClassResolver
import nl.adaptivity.android.kryo.serializers.*
import java.lang.ref.Reference
import kotlin.coroutines.CoroutineContext

open class AndroidKotlinResolver() : DefaultClassResolver() {

    override fun getRegistration(type: Class<*>): Registration? {
//        val c = context
        val superReg = super.getRegistration(type)
        return when {
            superReg != null -> superReg
            type.superclass == null -> superReg
            // For now this is actually unique, but this is not very stable.
//            c!=null && c.javaClass == type ->
//                register(Registration(type, ContextSerializer(context), NAME))
            Thread::class.java.isAssignableFrom(type) ->
                throw IllegalArgumentException("Serializing threads is never valid")
//            Context::class.java.isAssignableFrom(type.superclass) ->
//                register(Registration(type, ContextSerializer(context), NAME))
//            context is Activity && Fragment::class.java.isAssignableFrom(type.superclass) ->
//                register(Registration(type, FragmentSerializer(context), NAME))
//            context is FragmentActivity && android.support.v4.app.Fragment::class.java.isAssignableFrom(type.superclass) ->
//                register(Registration(type, SupportFragmentSerializer(context), NAME))
            Reference::class.java.isAssignableFrom(type) ->
                register(Registration(type, ReferenceSerializer(kryo, type.asSubclass(Reference::class.java)), NAME))
            Function::class.java.isAssignableFrom(type.superclass) ->
                register(
                    Registration(
                        type,
                        FieldSerializer<Any>(kryo, type).apply { fieldSerializerConfig.ignoreSyntheticFields = false },
                        NAME
                    )
                )
//            AccountManager::class.java.isAssignableFrom(type) ->
//                register(Registration(type, AccountManagerSerializer(kryo, type, c), NAME))
            // todo seems out of date?
            type.superclass?.name == "kotlin.coroutines.jvm.internal.ContinuationImpl" ->
                register(Registration(type, ContinuationImplSerializer(kryo, type), NAME))
            type.superclass?.name == "kotlin.coroutines.experimental.jvm.internal.CoroutineImpl" ->
                register(Registration(type, CoroutineImplSerializer(kryo, type), NAME))
//            type.superclass?.name == "kotlin.coroutines.SafeContinuation" ->
//                register(Registration(type, CoroutineImplSerializer(kryo, type), NAME))
//            type.name == "kotlin.coroutines.SafeContinuation" ->
//                register(Registration(type, CoroutineImplSerializer(kryo, type), NAME))
//            type.superclass?.name == "kotlin.sequences.SequenceBuilderIterator" ->
//                register(Registration(type, CoroutineImplSerializer(kryo, type), NAME))
// Requires the reflection library
//            type.kotlin.isCompanion -> register(Registration(type, kryo.pseudoObjectSerializer(type.kotlin.objectInstance), NAME))
            type.isKObject -> register(Registration(type, ObjectSerializer(kryo, type), NAME))
            else -> null
        }
    }

    companion object {
        const val TAG = "AndroidKotlinResolver"
        const val NAME = DefaultClassResolver.NAME.toInt()
        val APPCOMPATFRAGMENTCONTEXT_CLASS = try {
            Class.forName("nl.adaptivity.android.coroutinesCompat.AppcompatFragmentContext")
        } catch(e: ClassNotFoundException) {
            null
        }
        val APPCOMPATFRAGMENTCONTEXT_KEY: CoroutineContext.Key<*>? = APPCOMPATFRAGMENTCONTEXT_CLASS?.let { cl ->
            val inst = cl.getDeclaredField("Key")
            inst.get(null) as CoroutineContext.Key<*>
        }
    }
}