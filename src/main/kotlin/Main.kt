import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            var a = 1;
            if (a < 3 or true) {
                a = 4;  
            } else {
                a = 2;
            }
            
            print "Value of a: ";
            print a;
            """
    interpret(program)
}
