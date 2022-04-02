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

sealed class MatchResult
data class Matched(val token: ParserToken, val newCtx: ParsingContext) : MatchResult()
object Unmatched : MatchResult()

fun interface Rule {
    fun match(ctx: ParsingContext): MatchResult
}
typealias Combiner = (tokens: List<ParserToken>) -> ParserToken

val flatteningCombiner: Combiner = { tokens ->
    tokens
        .flatMap { if (it is MultipleParserToken) it.parserTokens else listOf(it) }
        .let { MultipleParserToken(it) }
}

fun orRule(vararg rules: Rule): Rule = Rule { ctx ->
    for (matcher in rules) {
        val result = matcher.match(ctx)
        if (result is Matched)
            return@Rule result
    }

    Unmatched
}

private fun andRule(rules: List<Rule>, combine: Combiner): Rule = Rule { ctx ->
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

fun andRule(vararg rules: Rule, combine: Combiner): Rule =
    andRule(rules.toList(), combine)

fun andRule(vararg rules: Rule): Rule =
    andRule(rules.toList(), flatteningCombiner)

fun zeroOrMoreRule(combine: Combiner, rule: Rule): Rule = Rule { ctx ->
    val matcherResults = mutableListOf<ParserToken>()
    var curCtx = ctx

    while (true) {
        val result = rule.match(curCtx)
        when (result) {
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

inline fun <reified T : LexerToken> singleTokenRule(): Rule =
    singleTokenRule<T, SingleParserToken> { SingleParserToken(it) }

inline fun <reified T : LexerToken, reified R : ParserToken> singleTokenRule(
    crossinline mapper: (T) -> R
): Rule = Rule { ctx ->
    val currentToken = ctx.currentToken()
    if (currentToken is T) {
        return@Rule Matched(mapper(currentToken), ctx.move())
    }
    Unmatched
}
