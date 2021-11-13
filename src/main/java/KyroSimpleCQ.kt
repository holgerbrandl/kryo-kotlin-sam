import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.ClosureSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import java.lang.invoke.SerializedLambda

class SmthgGeneric<T> {
    val foo: (T, T) -> String = { o1, o2 -> "foo" }
}

interface SmthgAbstractGeneric<T>  {
    fun foo(): (T, T) -> String
}

fun interface SmthgSAMGeneric<T> :Serializable  {
    fun foo(): (T, T) -> String
}

fun main() {
    // configure kryo
    val kryo = Kryo()
    kryo.setOptimizedGenerics(false);
    kryo.isRegistrationRequired = false
    //    kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
    //    kryo.setReferences(true)

    kryo.register(SerializedLambda::class.java)
    kryo.register(ClosureSerializer.Closure::class.java, ClosureSerializer())

    // start experimenting
    val smthg = SmthgGeneric<String>() // works

    kryo.saveRestore(smthg)

    val smthgElse = object: SmthgAbstractGeneric<String> {
        override fun foo(): (String, String) -> String {
            TODO("Not yet implemented")
        }
    }
    kryo.saveRestore(smthgElse) // works!


    // try a SAM (single abstract method interface)
    val samAsObject = object : SmthgSAMGeneric<String> {
        override fun foo(): (String, String) -> String {
            return { s: String, s2: String -> "42" }
        }
    }
    kryo.saveRestore(samAsObject) // work!s

    // finally, try the same but by expressing the SAM as lambda
    val inlineSam = SmthgSAMGeneric<String> {
        { s: String, s2: String -> "42" }
    }
    kryo.saveRestore(inlineSam) // fails with: Unable to find class: KyroSimpleCQKt$$Lambda$27/0x00000008000adc40
}

private fun Kryo.saveRestore(something: Any) {
    val saveFile = File("file.bin")
    saveFile.delete()

    val output = Output(FileOutputStream(saveFile))
    writeClassAndObject(output, something)
    output.close()

    println("shock-frosted ${something}")

    val input = Input(FileInputStream(saveFile));
    val restored = readClassAndObject(input)

    println("hooray we have restored ${restored}")
}
