import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            var a = 0;
            var b = 1;
            var isEven = true; // to overcome absence of % operator

            for (var i = 0; i < 50; i = i + 1) {
                var sum = a + b;
                print sum;

                if (isEven) {
                  a = sum;
                } else {
                  b = sum;
                }

                isEven = !isEven;
            }
            """
    interpret(program)
}
