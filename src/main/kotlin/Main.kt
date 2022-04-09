import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            var b = 12 - 5;
            var a = 1 + b / 3;
            print a;
            """
    interpret(program)
}
