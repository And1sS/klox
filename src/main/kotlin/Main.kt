import evaluator.evaluate
import lexer.tokenize
import parser.Expression
import parser.Matched
import parser.NodeToken
import parser.grammar
import parser.toParsingContext

fun main() {
    tokenize(
        """11 + (- 2) * (6 + 5)"""
            .trimIndent()
            .also(::println)
    ).also(::println)
        .toParsingContext()
        .let { grammar.match(it) }
        .let { it as Matched }.token
        .let { it as NodeToken }.node
        .let { it as Expression }
        .let(::evaluate)
        .also(::println)
}




