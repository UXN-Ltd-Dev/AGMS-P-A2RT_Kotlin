package kr.co.uxn.agms_p_a2rt.ble

/**
 * Keeps asynchronous Android GATT operations strictly serialized.
 * A failed head operation remains at the front until it succeeds or exhausts its retry budget.
 */
internal class GattOperationQueue<T>(
    private val maxAttempts: Int
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be greater than zero" }
    }

    data class Attempt<T>(
        val operation: T,
        val number: Int
    )

    enum class FailureAction {
        RETRY,
        EXHAUSTED
    }

    private data class Entry<T>(
        val operation: T,
        var attempts: Int = 0
    )

    private val entries = ArrayDeque<Entry<T>>()
    private var inFlight = false

    @Synchronized
    fun enqueue(operation: T): Int {
        entries.addLast(Entry(operation))
        return entries.size
    }

    @Synchronized
    fun nextAttempt(): Attempt<T>? {
        if (inFlight) return null

        val entry = entries.firstOrNull() ?: return null
        entry.attempts += 1
        inFlight = true
        return Attempt(entry.operation, entry.attempts)
    }

    @Synchronized
    fun inFlightOperation(): T? {
        return if (inFlight) entries.firstOrNull()?.operation else null
    }

    @Synchronized
    fun completeSuccess(): T {
        check(inFlight && entries.isNotEmpty()) { "No GATT operation is in flight" }
        val completed = entries.removeFirst().operation
        inFlight = false
        return completed
    }

    @Synchronized
    fun completeFailure(): FailureAction {
        check(inFlight && entries.isNotEmpty()) { "No GATT operation is in flight" }
        val entry = entries.first()
        inFlight = false

        return if (entry.attempts < maxAttempts) {
            FailureAction.RETRY
        } else {
            entries.removeFirst()
            FailureAction.EXHAUSTED
        }
    }

    @Synchronized
    fun clear() {
        entries.clear()
        inFlight = false
    }

    @Synchronized
    fun size(): Int = entries.size
}
