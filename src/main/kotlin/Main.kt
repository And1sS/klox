import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            fun returnFun() {
                var a = 1;
                fun f() {
                    a = a + 1;
                    print a;
                }
                //var a = 1;
                return f;
            }

            var a = 2;
            var f = returnFun();
            f();
            f();
            f();
            f();
            f();
            f();
            """
    interpret(program)
}
