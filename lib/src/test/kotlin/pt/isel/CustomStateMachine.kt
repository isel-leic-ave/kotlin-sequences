package pt.isel

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume


class Advancer(var cont: Continuation<Unit>?) {
    fun pause(step: Int) {
        println("Step $step")
    }
    fun advance() = cont?.resume(Unit)

}

fun suspendAndResume() : Advancer {
    return object : Callable<Advancer> {
        private var state = 0
        val adv = Advancer(object : Continuation<Unit> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Unit>) {
                call()
            }
        })
        override fun call(): Advancer {
            when(state) {
                0 -> {
                    state = 1
                    println("Starting ${this.hashCode()}")
                    adv.pause(state)
                }
                1 -> {
                    state = 2
                    println("Resuming ${this.hashCode()} in state 1")
                    adv.pause(state)
                }
                2 -> {
                    state = 3
                    println("Resuming ${this.hashCode()} in state 2")
                    adv.pause(state)
                }
            }
            return adv
        }
    }
    .call()
}

class CustomStateMachine {
    @Test fun checkSuspendAndResume() {
        val adv1: Advancer = suspendAndResume()
        val adv2: Advancer = suspendAndResume()
        adv1.advance()
        adv2.advance()
        adv1.advance()
        adv2.advance()
    }
}