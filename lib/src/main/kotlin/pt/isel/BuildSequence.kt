package pt.isel

import pt.isel.SequenceState.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume

abstract class Yieldable<T> {
    val yield = ::yieldHandler as (suspend (T) -> Unit)

    protected abstract fun yieldHandler(item: T, cont: Continuation<Unit>) : Any
}

enum class SequenceState { NotReady, Ready, Done, Failed}

fun <T> buildSequence(block: suspend Yieldable<T>.() -> Unit) = object : Sequence<T> {
    private var nextItem: T? = null
    private var nextStep: Continuation<Unit>? = null
    private var state = NotReady
    private val blockCps = block as Yieldable<T>.(Continuation<Unit>) -> Unit
    /**
     * This scope will be the receiver of the block function providing it the yield function().
     * The yield() redirects to the next yieldHandler() implementation, which collects the nextItem,
     * the next continuation and returns the suspended state.
     */
    private val scope = object : Yieldable<T>() {
        override fun yieldHandler(item: T, cont: Continuation<Unit>) : Any {
            nextItem = item
            nextStep = cont
            state = Ready
            /*
             * It means that suspend function did suspend the execution
             * and will not return any result immediately.
             */
            return COROUTINE_SUSPENDED
        }
    }
    /**
     * The onCompletion Continuation called when the block function (same as blockCps)
     * finishes its execution.
     */
    private val onCompletion = object: Continuation<Unit> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<Unit>) {
            state = if(result.isSuccess) { Done } else { Failed }
        }
    }

    private fun start() = scope.blockCps(onCompletion)

    override fun iterator() = object : Iterator<T> {

        private fun tryAdvance() {
            if(state == NotReady) {
                nextStep
                    ?.resume(Unit)  // On next steps
                    ?: start()      // On first step
            }
        }

        override fun hasNext(): Boolean {
            tryAdvance()
            return state == Ready
        }

        override fun next(): T {
            if(!hasNext()) {
                throw NoSuchElementException()
            }
            state = NotReady
            /*
             * I don't like the next cast.
             * However, is similar to what is being done in Kotlin SequenceBuilderIterator
             * https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/collections/SequenceBuilder.kt
             */
            @Suppress("UNCHECKED_CAST")
            return nextItem as T
        }
    }
}

