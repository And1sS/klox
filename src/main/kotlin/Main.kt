import interpreter.interpret

fun main() {
    // TODO: add error synchronization
    val program = """
            fun fib(n) {
                if (n == 0) return 0;
                if (n == 1) return 1;
                return fib(n - 1) + fib(n - 2);
            }
            
            fun print_n_first_fib(n) {
                for (var i = 0; i < n; i = i + 1) {
                    print(fib(i));
                }
            }
            
            var before = clock();
            print fib(30);
            var after = clock();
            print after - before;
            """
    interpret(program)
}
