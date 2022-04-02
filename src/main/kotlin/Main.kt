import lexer.tokenize

fun main() {
    tokenize(
        """
            class Breakfast {
              cook() {
                print "Eggs a-fryin'!";
              }
            
              serve(who) {
                print "Enjoy your breakfast, " + who + ".";
              }
            }
        """.trimIndent().also(::println)
    ).also(::println)
}



