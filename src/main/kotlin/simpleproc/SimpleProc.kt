package simpleproc

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.ClosureSerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import java.lang.invoke.SerializedLambda


class SimpleProcess(seq: Sequence<String>) : Serializable {

    val iterator = seq.iterator()
}


fun main() {
    val sim = SimpleProcess(sequence {
        yield("foo")
        yield("bar")
    })

    // consume the sequence once
    println(sim.iterator.next())

    val kryo = buildProcKryo()

    val saveFile = File("file.bin")

    val output = Output(FileOutputStream(saveFile))
    kryo.writeClassAndObject(output, sim)
    output.close()

    val input = Input(FileInputStream(saveFile));
    val restored = kryo.readClassAndObject(input) as SimpleProcess

    // consume it again
    println(restored.iterator.next())

    // this fails with
//    Exception in thread "main" java.lang.NullPointerException
//    at simpleproc.SimpleProcKt$main$sim$1.invokeSuspend(SimpleProc.kt:25)
//    at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
//    at kotlin.sequences.SequenceBuilderIterator.hasNext(SequenceBuilder.kt:140)
//    at kotlin.sequences.SequenceBuilderIterator.nextNotReady(SequenceBuilder.kt:163)
//    at kotlin.sequences.SequenceBuilderIterator.next(SequenceBuilder.kt:146)
//    at simpleproc.SimpleProcKt.main(SimpleProc.kt:43)
//    at simpleproc.SimpleProcKt.main(SimpleProc.kt)
}

fun buildProcKryo(): Kryo {
    val kryo = Kryo()

    kryo.setOptimizedGenerics(false);
    kryo.setReferences(true)

    kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
    kryo.isRegistrationRequired = false

    kryo.register(SerializedLambda::class.java)
    kryo.register(ClosureSerializer.Closure::class.java, ClosureSerializer())

    return kryo
}