package nl.adaptivity.android.kryo.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

/**
 * Serializer for Kotlin objects that stores nothing and just retrieves the current instance from
 * the field.
 */
internal class PseudoObjectSerializer<T>(kryo: Kryo, val type: Class<T>, val value: T): Serializer<T>(false, true) {

    override fun write(kryo: Kryo, output: Output, obj: T?) {
        // The class is already written by the caller so no need to write anything
    }

    override fun read(kryo: Kryo?, input: Input?, type: Class<out T>?): T {
        return value
    }
}

internal inline fun <reified T> Kryo.pseudoObjectSerializer(value:T) = PseudoObjectSerializer(this, T::class.java, value)