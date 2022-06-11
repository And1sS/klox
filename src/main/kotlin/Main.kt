import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    // TODO: think about splitting AST tree and runtime values into 2 different class hierarchies
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
