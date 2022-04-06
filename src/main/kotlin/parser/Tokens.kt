package parser

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

sealed class AbstractSyntaxNode()

sealed class Expression : AbstractSyntaxNode()

sealed class Value : Expression()

object NilValue : Value()

data class BooleanValue(val value: Boolean) : Value()

data class NumericValue(val value: Double) : Value()

class StringValue(val value: String) : Value()

class ObjectValue() : Value()

data class UnaryOperatorExpression(
    val operatorType: OperatorType,
    val expr: Expression
) : Expression()

data class BinaryOperatorExpression(
    val operatorType: OperatorType,
    val lhs: Expression,
    val rhs: Expression
) : Expression()

class IdentifierExpression(token: IdentifierLexerToken) : Expression() {
    val name: String = token.name

    override fun toString(): String = "IdentifierExpression(name = $name)"
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