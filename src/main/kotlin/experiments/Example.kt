package experiments

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import kotlinx.coroutines.runBlocking
import nl.adaptivity.android.kryo.serializers.KryoAndroidConstants
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.Continuation

// Adopted from https://gist.github.com/Restioson/fb5b92e16eaff3d9267024282cf1ed72
// Note: not working, because of unclear migration from old coroutine-API
fun main(args: Array<String>) {

//    if (args.size>0) {
        runBlocking {
            println("Launching")
            SerialisationHelper.test()
            println("Finished")
        }
//    }

//    else {
        val coro = SerialisationHelper.deserialiseCoro()
        coro.resumeWith(Result.failure(RuntimeException()))
//    }

    while (true) {}

}

object SerialisationHelper {

    val kryo = Kryo()

    init {
        kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
    }

    suspend fun test(): Unit = runBlocking {
        println("Serialising")
        // todo reenable
//        testSerialise(this)
    }

    fun <T: Any> serialiseCoro(coro: Continuation<T>) {
        val output = Output(FileOutputStream("coro.bin"))
        kryo.writeClassAndObject(output, coro)
        output.close()
    }

    fun deserialiseCoro(): Continuation<*> {

        println("Deserialising")

        val fis = Input(FileInputStream("coro.bin"))

        val coro = kryo.readClassAndObject(fis) as Continuation<*>
        fis.close()

        coro::class.java.getDeclaredField("result").apply {
            isAccessible = true
            set(coro, KryoAndroidConstants.COROUTINE_SUSPENDED)
        }

        return coro
    }

    fun <T: Any> testSerialise(coro: Continuation<T>) {
        serialiseCoro(coro)
    }

}