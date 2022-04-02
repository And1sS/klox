package parser

import lexer.BangEqualLexerToken
import lexer.IdentifierLexerToken
import lexer.LexerToken
import lexer.BangLexerToken
import lexer.EqualEqualLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.MinusLexerToken
import lexer.PlusLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import lexer.StringLiteralLexerToken
import kotlin.reflect.KClass

sealed class ParserToken()

data class SingleParserToken(val lexerToken: LexerToken) : ParserToken()
data class MultipleParserToken(val parserTokens: List<ParserToken>) : ParserToken()

sealed class Expression : ParserToken()

class IdentifierExpression(token: IdentifierLexerToken) : Expression() {
    val name: String = token.name

    override fun toString(): String = "IdentifierExpression(name = $name)"
}

class StringLiteralExpression(token: StringLiteralLexerToken) : Expression() {
    val value: String = token.value

    override fun toString(): String = "StringLiteralExpression(value = $value)"
}

data class NumberLiteralExpression(val value: Double) : Expression()

data class BooleanLiteralExpression(val value: Boolean) : Expression()

object NilExpression : Expression()

data class UnaryOperatorExpression(
    val operatorType: OperatorType,
    val expr: Expression
) : Expression()

data class BinaryOperatorExpression(
    val operatorType: OperatorType,
    val lhs: Expression,
    val rhs: Expression
) : Expression()

enum class OperatorType {
    Bang,
    Plus,
    Minus,
    Star,
    Slash,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,
    BangEqual,
    EqualEqual
}

val operatorMapping = mapOf<KClass<out LexerToken>, OperatorType>(
    BangLexerToken::class to OperatorType.Bang,
    PlusLexerToken::class to OperatorType.Plus,
    MinusLexerToken::class to OperatorType.Minus,
    StarLexerToken::class to OperatorType.Star,
    SlashLexerToken::class to OperatorType.Slash,
    GreaterLexerToken::class to OperatorType.Greater,
    GreaterEqualLexerToken::class to OperatorType.GreaterEqual,
    LessLexerToken::class to OperatorType.Less,
    LessEqualLexerToken::class to OperatorType.LessEqual,
    BangEqualLexerToken::class to OperatorType.BangEqual,
    EqualEqualLexerToken::class to OperatorType.EqualEqual
)