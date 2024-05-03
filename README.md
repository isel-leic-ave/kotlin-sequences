## buildSequence()

Equivalent to:
```kotlin
fun <T> sequence(
    block: suspend SequenceScope<T>.() -> Unit
): Sequence<T>
```

For `buildSequence` we have:
```kotlin
fun <T> buildSequence(
    block: suspend Yieldable<T>.() -> Unit
): Sequence<T>
```

