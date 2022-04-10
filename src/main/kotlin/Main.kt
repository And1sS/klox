import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            var b = 12 - 5;
            print b;
            {
                var c = 3;
                var b = b + 1;
                print b;
            }
            
            var d = 3;
            //print c + d;
            """
    interpret(program)
}
