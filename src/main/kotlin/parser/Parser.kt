package parser

import ast.AbstractSyntaxNode
import ast.Declaration
import lexer.CommaLexerToken
import lexer.LexerToken
import parser.rules.binaryOperatorRule
import parser.rules.commaRule

data class ParsingContext(val tokens: List<LexerToken>, val currentIndex: Int) {
    fun move(): ParsingContext = ParsingContext(tokens, currentIndex + 1)

    fun currentToken(): LexerToken = tokens[currentIndex]
}

fun List<LexerToken>.toParsingContext(): ParsingContext =
    ParsingContext(this, 0)

sealed class ParserToken

data class SymbolicToken(val lexerToken: LexerToken) : ParserToken()
data class NodeToken(val node: AbstractSyntaxNode) : ParserToken()
data class CompositeToken(val tokens: List<ParserToken>) : ParserToken()
data class ProgramToken(val declarations: List<Declaration>) : ParserToken() {
    override fun toString(): String =
        """"ProgramToken(
            | declarations = 
            | ${declarations.joinToString("\n")}
            |)""".trimMargin()
}

data class OptionalToken(val token: ParserToken?) : ParserToken() {
    companion object {
        fun empty(): OptionalToken = OptionalToken(null)
        fun value(token: ParserToken): OptionalToken = OptionalToken(token)
    }
}

sealed class MatchResult
data class Matched(val token: ParserToken, val newCtx: ParsingContext) : MatchResult()
object Unmatched : MatchResult()

typealias Combiner = (tokens: List<ParserToken>) -> ParserToken

val flatteningCombiner: Combiner = { tokens ->
    tokens
        .flatMap { if (it is CompositeToken) it.tokens else listOf(it) }
        .let { CompositeToken(it) }
}

fun interface Rule {
    fun match(ctx: ParsingContext): MatchResult
}

fun orRule(vararg rules: Rule): Rule = Rule { ctx ->
    for (matcher in rules) {
        val result = matcher.match(ctx)
        if (result is Matched)
            return@Rule result
    }

    Unmatched
}

fun andRule(vararg rules: Rule, combine: Combiner): Rule = Rule { ctx ->
    val matcherResults = mutableListOf<ParserToken>()
    var curCtx = ctx

    for (rule in rules) {
        when (val result = rule.match(curCtx)) {
            is Unmatched -> return@Rule Unmatched
            is Matched -> {
                matcherResults.add(result.token)
                curCtx = result.newCtx
            }
        }
    }

    Matched(combine(matcherResults), curCtx)
}

fun andRule(vararg rules: Rule): Rule =
    andRule(*rules) { flatteningCombiner(it) }

fun optionalRule(rule: Rule): Rule = Rule { ctx ->
    when (val result = rule.match(ctx)) {
        is Unmatched -> Matched(OptionalToken.empty(), ctx)
        is Matched -> Matched(OptionalToken.value(result.token), result.newCtx)
    }
}

fun zeroOrMoreRule(rule: Rule, combine: Combiner): Rule = Rule { ctx ->
    val matcherResults = mutableListOf<ParserToken>()
    var curCtx = ctx

    while (true) {
        when (val result = rule.match(curCtx)) {
            is Unmatched -> return@Rule Matched(combine(matcherResults), curCtx)
            is Matched -> {
                matcherResults.add(result.token)
                curCtx = result.newCtx
            }
        }
    }

    Unmatched // Should never occur, just to satisfy compiler
}

fun zeroOrMoreRule(rule: Rule): Rule =
    zeroOrMoreRule(rule, flatteningCombiner)

inline fun <reified T> listRule(operandRule: Rule): Rule =
    binaryOperatorRule(operandRule, commaRule) { tokens ->
        val arguments = tokens.filterNot { it is SymbolicToken && it.lexerToken is CommaLexerToken }
        validateGrammar(arguments.all { it is NodeToken && it.node is T })

        CompositeToken(arguments)
    }

inline fun <reified T : LexerToken> symbolicTokenRule(): Rule =
    singleTokenRule<T, SymbolicToken>(::SymbolicToken)

inline fun <reified T : LexerToken> nodeTokenRule(
    crossinline mapper: (T) -> AbstractSyntaxNode
): Rule = singleTokenRule<T, NodeToken> { NodeToken(mapper(it)) }

inline fun <reified T : LexerToken, reified R : ParserToken> singleTokenRule(
    crossinline mapper: (T) -> R
): Rule = Rule { ctx ->
    val currentToken = ctx.currentToken()
    if (currentToken is T) {
        return@Rule Matched(mapper(currentToken), ctx.move())
    }
    Unmatched
}
