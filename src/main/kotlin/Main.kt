import lexer.tokenize

fun main() {
    tokenize(
        """
            fun () {
              var = ;

              fun () {
                print ;
              }

              return ;
            }

            var  = ();
            ();
        """.trimIndent()
    ).also(::println)
}



