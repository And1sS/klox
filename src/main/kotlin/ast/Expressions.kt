package ast

import lexer.AndLexerToken
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EqualEqualLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.LexerToken
import lexer.MinusLexerToken
import lexer.OrLexerToken
import lexer.PlusLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import kotlin.reflect.KClass

sealed class Expression : AbstractSyntaxNode()

sealed class Literal : Expression()

object NilLiteral : Literal()

data class BooleanLiteral(val value: Boolean) : Literal()

data class NumericLiteral(val value: Double) : Literal()

data class StringLiteral(val value: String) : Literal()

data class CallExpression(
    val function: Expression,
    val arguments: List<Expression>
) : Expression()

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

sealed class IdentifierExpression(val name: String) : Expression()
class UnresolvedIdentifierExpression(name: String) : IdentifierExpression(name)
class ResolvedIdentifierExpression(name: String, val depth: Int) : IdentifierExpression(name)

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
    EqualEqual,
    Or,
    And
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
    EqualEqualLexerToken::class to OperatorType.EqualEqual,
    OrLexerToken::class to OperatorType.Or,
    AndLexerToken::class to OperatorType.And
)
