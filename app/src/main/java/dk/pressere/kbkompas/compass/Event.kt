package dk.pressere.kbkompas.compass

class Event<out T> (private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContent():T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent():T {return content}
}