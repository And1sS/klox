import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            var a = 1;
            var b;
            print a;
            {
                print a = b = 2;
                print a + b;
            }
            print a;
            """
    interpret(program)
}
