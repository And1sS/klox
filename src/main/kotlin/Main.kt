import lexer.tokenize
import parser.grammar
import parser.toParsingContext

fun main() {
    tokenize(
        """!1 + (- 2) * (6 + 5) > 15"""
            .trimIndent()
            .also(::println)
    ).also(::println)
        .toParsingContext()
        .let { grammar.match(it) }
        .also(::println)
}




