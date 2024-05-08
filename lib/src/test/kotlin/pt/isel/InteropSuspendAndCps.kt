package pt.isel

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.test.assertEquals

/**
 * Check interoperability between a normal function with a callback, i.e. CPS (Continuation-passing style)
 * and a Kotlin suspend function
 */
class InteropSuspendAndCps {
    private suspend fun increment(nr: Int): Int {
        delay(10)
        return nr + 1
    }

    private fun incrementCps(nr: Int, onCompletion: Continuation<Int>) : Any {
        onCompletion.resume(nr + 1)
        return COROUTINE_SUSPENDED // it means that suspend function did suspend the execution and will not return any result immediately
    }

    @Test
    fun callSuspendAsCpsFunction() {
        val incHandler = ::increment as (Int, Continuation<Int>) -> Unit
        val res = CompletableFuture<Int>()
        incHandler(7, object : Continuation<Int>{
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                res.complete(result.getOrThrow())
            }
        })
        assertEquals(8, res.join())
    }

    @Test
    fun callCpsFunctionAsSuspend() = runBlocking {
        val incHandler = ::incrementCps as (suspend (Int) -> Int)
        val res = incHandler(7)
        assertEquals(8, res)
    }
}