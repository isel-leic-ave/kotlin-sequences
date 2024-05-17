package pt.isel

import java.net.URI
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


fun fetchManyCps(url1: String, url2: String, url3: String, onComplete: Continuation<List<String>>) {
    FetchManyCps(url1, url2, url3, onComplete)
        .resume(null)
}

class FetchManyCps(
    private val url1: String,
    private val url2: String,
    private val url3: String,
    private val onComplete: Continuation<List<String>>
)
    : Continuation<String?>
{
    private val bodies = mutableListOf<String>()
    var state = 0

    override val context = EmptyCoroutineContext

    override fun resumeWith(result: Result<String?>) {
        result.getOrThrow()?.let { bodies.add(it) }
        when(state++) {
            0 -> fetchCps(url1, this)
            1 -> fetchCps(url2, this)
            2 -> fetchCps(url3, this)
            3 -> onComplete.resume(bodies)
        }
    }
}









