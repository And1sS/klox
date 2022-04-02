package parser

import binaryOperatorRule
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EqualEqualLexerToken
import lexer.FalseLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.IdentifierLexerToken
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

val identifierRule =
    singleTokenRule<IdentifierLexerToken, IdentifierExpression> { IdentifierExpression(it) }
val stringLiteralRule =
    singleTokenRule<StringLiteralLexerToken, StringLiteralExpression> { StringLiteralExpression(it) }
val numberLiteralRule =
    singleTokenRule<NumberLiteralLexerToken, NumberLiteralExpression> { NumberLiteralExpression(it.value) }

val trueRule = singleTokenRule<TrueLexerToken, BooleanLiteralExpression> { BooleanLiteralExpression(true) }
val falseRule = singleTokenRule<FalseLexerToken, BooleanLiteralExpression> { BooleanLiteralExpression(false) }
val nilRule = singleTokenRule<NilLexerToken, NilExpression> { NilExpression }

val lparenRule = singleTokenRule<LeftParenLexerToken>()
val rparenRule = singleTokenRule<RightParenLexerToken>()

val bangRule: Rule = singleTokenRule<BangLexerToken>()
val plusRule: Rule = singleTokenRule<PlusLexerToken>()
val minusRule: Rule = singleTokenRule<MinusLexerToken>()
val starRule: Rule = singleTokenRule<StarLexerToken>()
val slashRule: Rule = singleTokenRule<SlashLexerToken>()

val greaterRule: Rule = singleTokenRule<GreaterLexerToken>()
val greaterEqualRule: Rule = singleTokenRule<GreaterEqualLexerToken>()
val lessRule: Rule = singleTokenRule<LessLexerToken>()
val lessEqualRule: Rule = singleTokenRule<LessEqualLexerToken>()

val bangEqualRule: Rule = singleTokenRule<BangEqualLexerToken>()
val equalEqualRule: Rule = singleTokenRule<EqualEqualLexerToken>()

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
        tokens.toOperatorTypeAndOperand().let { UnaryOperatorExpression(it.first, it.second) }
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
