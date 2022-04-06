package parser

import binaryOperatorRule
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EqualEqualLexerToken
import lexer.FalseLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.LeftParenLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.MinusLexerToken
import lexer.NilLexerToken
import lexer.NumberLiteralLexerToken
import lexer.PlusLexerToken
import lexer.RightParenLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import lexer.StringLiteralLexerToken
import lexer.TrueLexerToken

val identifierRule = nodeTokenRule(::IdentifierExpression)
val stringLiteralRule = nodeTokenRule<StringLiteralLexerToken> { StringValue(it.value) }
val numberLiteralRule = nodeTokenRule<NumberLiteralLexerToken> { NumericValue(it.value) }

val trueRule = nodeTokenRule<TrueLexerToken> { BooleanValue(true) }
val falseRule = nodeTokenRule<FalseLexerToken> { BooleanValue(false) }
val nilRule = nodeTokenRule<NilLexerToken> { NilValue }

val lparenRule = symbolicTokenRule<LeftParenLexerToken>()
val rparenRule = symbolicTokenRule<RightParenLexerToken>()

val bangRule: Rule = symbolicTokenRule<BangLexerToken>()
val plusRule: Rule = symbolicTokenRule<PlusLexerToken>()
val minusRule: Rule = symbolicTokenRule<MinusLexerToken>()
val starRule: Rule = symbolicTokenRule<StarLexerToken>()
val slashRule: Rule = symbolicTokenRule<SlashLexerToken>()

val greaterRule: Rule = symbolicTokenRule<GreaterLexerToken>()
val greaterEqualRule: Rule = symbolicTokenRule<GreaterEqualLexerToken>()
val lessRule: Rule = symbolicTokenRule<LessLexerToken>()
val lessEqualRule: Rule = symbolicTokenRule<LessEqualLexerToken>()

val bangEqualRule: Rule = symbolicTokenRule<BangEqualLexerToken>()
val equalEqualRule: Rule = symbolicTokenRule<EqualEqualLexerToken>()

private val expressionRule = Rule { ctx ->
    equalityRule.match(ctx)
}

val grammar: Rule = expressionRule

// "(" expression ")"
private val parenthesizedRule: Rule = andRule(lparenRule, expressionRule, rparenRule) { it[1] }

// primaryExpression -> NUMBER | STRING | "true" | "false" | "nil" | parenthesizedRule
private val primaryExpressionRule = orRule(
    numberLiteralRule,
    stringLiteralRule,
    trueRule,
    falseRule,
    nilRule,
    parenthesizedRule
)

private val intermediateUnaryRule = Rule { ctx ->
    andRule(orRule(minusRule, bangRule), unaryOperatorRule) { tokens ->
        tokens
            .toOperatorTypeAndOperand()
            .let { (type, operand) -> UnaryOperatorExpression(type, operand) }
            .let(::NodeToken)
    }.match(ctx)
}

// unaryOperator -> ( "!" | "-" ) unaryOperator | primaryExpression
private val unaryOperatorRule: Rule = orRule(intermediateUnaryRule, primaryExpressionRule)

// factor -> unary ( ( "/" | "*" ) unary )*
private val factorRule: Rule = binaryOperatorRule(unaryOperatorRule, orRule(starRule, slashRule))

// term -> factor ( ( "-" | "+" ) factor )*
private val termRule: Rule = binaryOperatorRule(factorRule, orRule(plusRule, minusRule))

// comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
private val comparisonRule: Rule = binaryOperatorRule(
    termRule,
    orRule(greaterRule, greaterEqualRule, lessRule, lessEqualRule)
)

// equality -> comparison ( ( "!=" | "==" ) comparison )*
private val equalityRule: Rule = binaryOperatorRule(comparisonRule, orRule(bangEqualRule, equalEqualRule))
