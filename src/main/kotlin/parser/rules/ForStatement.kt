package parser.rules

import lexer.SemicolonLexerToken
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.SymbolicToken
import parser.andRule
import parser.ast.Expression
import parser.ast.ExpressionStatement
import parser.ast.ForStatement
import parser.ast.Statement
import parser.ast.VarDeclaration
import parser.component6
import parser.component7
import parser.component8
import parser.optionalRule
import parser.orRule
import parser.throwInvalidGrammar
import parser.validateGrammar

// forStatement -> "for" "(" (varDeclaration | expressionStatement | ";") expression? ";" expression? ")" statement
val forStatementRule: Rule = andRule(
    forKeywordRule, leftParenRule,
    orRule(varDeclarationRule, expressionStatementRule, semicolonRule),
    optionalRule(expressionRule), semicolonRule,
    optionalRule(expressionRule), rightParenRule,
    intermediateStatementRule
) { tokens ->
    val (_, _, initializerToken, optionalConditionToken, _, optionalIncrementToken, _, bodyToken) = tokens

    var varDeclarationInitializer: VarDeclaration? = null
    var expressionStatementInitializer: ExpressionStatement? = null
    when (initializerToken) {
        is NodeToken -> when (initializerToken.node) {
            is VarDeclaration -> varDeclarationInitializer = initializerToken.node
            is ExpressionStatement -> expressionStatementInitializer = initializerToken.node
            else -> throwInvalidGrammar()
        }
        else -> validateGrammar(
            initializerToken is SymbolicToken
                    && initializerToken.lexerToken is SemicolonLexerToken
        )
    }

    validateGrammar(optionalConditionToken is OptionalToken)
    val conditionExpression: Expression? = optionalConditionToken.token?.let { conditionToken ->
        validateGrammar(conditionToken is NodeToken && conditionToken.node is Expression)

        conditionToken.node
    }

    validateGrammar(optionalIncrementToken is OptionalToken)
    val incrementExpression: Expression? = optionalIncrementToken.token?.let { incrementToken ->
        validateGrammar(incrementToken is NodeToken && incrementToken.node is Expression)

        incrementToken.node
    }

    validateGrammar(bodyToken is NodeToken && bodyToken.node is Statement)

    val forStatement = if (varDeclarationInitializer != null) {
        ForStatement(varDeclarationInitializer, conditionExpression, incrementExpression, bodyToken.node)
    } else {
        ForStatement(expressionStatementInitializer, conditionExpression, incrementExpression, bodyToken.node)
    }

    NodeToken(forStatement)
}
