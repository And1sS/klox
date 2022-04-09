package parser

import binaryOperatorRule
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EOFLexerToken
import lexer.EqualEqualLexerToken
import lexer.EqualLexerToken
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
import lexer.PrintLexerToken
import lexer.RightParenLexerToken
import lexer.SemicolonLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import lexer.StringLiteralLexerToken
import lexer.TrueLexerToken
import lexer.VarLexerToken
import parser.rules.varDeclarationRule

val identifierRule = nodeTokenRule(::IdentifierExpression)
val stringLiteralRule = nodeTokenRule<StringLiteralLexerToken> { StringValue(it.value) }
val numberLiteralRule = nodeTokenRule<NumberLiteralLexerToken> { NumericValue(it.value) }

val printRule = symbolicTokenRule<PrintLexerToken>()
val varRule = symbolicTokenRule<VarLexerToken>()

val trueRule = nodeTokenRule<TrueLexerToken> { BooleanValue(true) }
val falseRule = nodeTokenRule<FalseLexerToken> { BooleanValue(false) }
val nilRule = nodeTokenRule<NilLexerToken> { NilValue }

val semicolonRule = symbolicTokenRule<SemicolonLexerToken>()

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

val equalRule: Rule = symbolicTokenRule<EqualLexerToken>()
val bangEqualRule: Rule = symbolicTokenRule<BangEqualLexerToken>()
val equalEqualRule: Rule = symbolicTokenRule<EqualEqualLexerToken>()

val eofRule: Rule = symbolicTokenRule<EOFLexerToken>()

val expressionRule = Rule { ctx ->
    equalityRule.match(ctx)
}

val grammar: Rule = expressionRule

// TODO: refactor rules ordering
val printStatementRule: Rule = andRule(printRule, expressionRule, semicolonRule) { tokens ->
    val (_, expressionToken, _) = tokens
    require(expressionToken is NodeToken && expressionToken.node is Expression) {
        "Invalid grammar"
    }
    NodeToken(PrintStatement(expressionToken.node))
}

val expressionStatementRule: Rule = andRule(expressionRule, semicolonRule) { tokens ->
    val (expressionToken, _) = tokens
    require(expressionToken is NodeToken && expressionToken.node is Expression) {
        "Invalid grammar"
    }
    NodeToken(ExpressionStatement(expressionToken.node))
}

val statementRule: Rule = orRule(expressionStatementRule, printStatementRule)

var declarationRule = orRule(varDeclarationRule, statementRule)

val programRule: Rule = andRule(zeroOrMoreRule(declarationRule), eofRule) { tokens ->
    val flattenedTokens = flatteningCombiner(tokens)
    require(flattenedTokens is CompositeToken) { "Invalid grammar" }
    flattenedTokens.tokens
        .dropLast(1)
        .map { token ->
            require(token is NodeToken && token.node is Declaration) { "Invalid grammar" }
            token.node
        }.let(::ProgramToken)
}

// "(" expression ")"
private val parenthesizedRule: Rule = andRule(lparenRule, expressionRule, rparenRule) { it[1] }

// primaryExpression -> NUMBER | STRING | "true" | "false" | "nil" | parenthesizedRule
private val primaryExpressionRule = orRule(
    numberLiteralRule,
    stringLiteralRule,
    trueRule,
    falseRule,
    nilRule,
    parenthesizedRule,
    identifierRule
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
