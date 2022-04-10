package parser.ast

import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EqualEqualLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.IdentifierLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.LexerToken
import lexer.MinusLexerToken
import lexer.PlusLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import kotlin.reflect.KClass

sealed class Expression : AbstractSyntaxNode()

sealed class Value : Expression()

object NilValue : Value()

data class BooleanValue(val value: Boolean) : Value()

data class NumericValue(val value: Double) : Value()

data class StringValue(val value: String) : Value()

// TODO: implement objects properly
object ObjectValue : Value()

data class AssignmentExpression(
    val identifier: IdentifierExpression,
    val expr: Expression
) : Expression()

data class UnaryOperatorExpression(
    val operatorType: OperatorType,
    val expr: Expression
) : Expression()

data class BinaryOperatorExpression(
    val operatorType: OperatorType,
    val lhs: Expression,
    val rhs: Expression
) : Expression()

data class IdentifierExpression private constructor(val name: String) : Expression() {
    constructor(token: IdentifierLexerToken) : this(token.name)
}

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

val operatorMapping: Map<KClass<out LexerToken>, OperatorType> = mapOf(
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