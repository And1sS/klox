package parser

import lexer.LexerToken

data class ParsingContext(val tokens: List<LexerToken>, val currentIndex: Int) {
    fun move(amount: Int): ParsingContext =
        ParsingContext(tokens, currentIndex + amount)

    fun move(): ParsingContext = move(1)

    fun currentToken(): LexerToken = tokens[currentIndex]
}

fun List<LexerToken>.toParsingContext(): ParsingContext =
    ParsingContext(this, 0)

sealed class ParserToken

data class SymbolicToken(val lexerToken: LexerToken) : ParserToken()
data class NodeToken(val node: AbstractSyntaxNode) : ParserToken()
data class CompositeToken(val tokens: List<ParserToken>) : ParserToken()

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

fun zeroOrMoreRule(combine: Combiner, rule: Rule): Rule = Rule { ctx ->
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
    zeroOrMoreRule(flatteningCombiner, rule)

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
