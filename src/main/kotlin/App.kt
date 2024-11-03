import model.CommandHandler


fun main() {
    val handler = CommandHandler()
    while (true) {
        print("> ")
        val command = readLine() ?: break
        handler.executeCommand(command)
    }
}
