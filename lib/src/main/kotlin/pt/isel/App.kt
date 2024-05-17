package pt.isel

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

fun main() {
    val url1 = "https://stackoverflow.com/"
    val url2 = "https://github.com/"
    val url3 = "https://developer.mozilla.org/"
    testFetchMany(url1, url2, url3)
    testFetchManyCps(url1, url2, url3)
    testFetchManyCpsLazy(url1, url2, url3)
    testFetchManySuspendLazy(url1, url2, url3)
    testFetchManySuspendNative(url1, url2, url3)
}


fun testFetchMany(url1: String, url2: String, url3: String) {
    println("##### Testing fetchMany")
    fetchMany(url1, url2, url3)
    println(">>>>> Call to fetchMany finished!")
}

fun testFetchManyCps(url1: String, url2: String, url3: String) {
    println("##### Testing fetchManyCps")
    fetchManyCps(url1, url2, url3,
        object : Continuation<List<String>> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<List<String>>) {
                result
                    .getOrThrow()
                    .also { println("Finish!") }
            }
        }
    )
    println(">>>>> Call to fetchManyCps finished!")
}

fun testFetchManyCpsLazy(url1: String, url2: String, url3: String) {
    println("##### Testing fetchManyCpsLazy")
    val iter = fetchManyCpsLazy (url1, url2, url3)
        .iterator()
    println(">>>>> Call to fetchManyCpsLazy finished!")
    iter.next()
    iter.next()
}

fun testFetchManySuspendLazy(url1: String, url2: String, url3: String) {
    println("##### Testing fetchManySuspendLazy")
    val iter = fetchManySuspendLazy(url1, url2, url3)
        .iterator()
    println(">>>>> Call to fetchManySuspendLazy finished!")
    iter.next()
    iter.next()
}

fun testFetchManySuspendNative(url1: String, url2: String, url3: String) {
    println("##### Testing fetchManySuspendNative")
    val iter = fetchManySuspend(url1, url2, url3)
        .iterator()
    println(">>>>> Call to fetchManySuspendNative finished!")
    iter.next()
    iter.next()
}


fun fetchManySuspend(url1: String, url2: String, url3: String) : Sequence<String> {
    return sequence {
        val body1 = fetch(url1)
        yield(body1)
        val body2 = fetch(url2)
        yield(body2)
        val body3 = fetch(url3)
        yield(body3)
    }
}