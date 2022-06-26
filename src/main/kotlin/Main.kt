import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
        //var x = 1;
        class A {
            var y = b();
            var x = 2;

            fun A() {
                print y;
                print "constructor";
                b();
            }
            
            fun b() {
                return x;
            }
        }

        var a = A();
            """
    interpret(program)
}
