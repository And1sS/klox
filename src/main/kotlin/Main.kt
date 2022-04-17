import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            fun test(a, b) {
                var c = a + b;
                print c;
            }
            var c = test;
            c(1, 5);
            """
    interpret(program)
}
