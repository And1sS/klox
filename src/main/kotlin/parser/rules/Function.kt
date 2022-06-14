package parser.rules

import ast.BlockStatement
import ast.Expression
import ast.FunctionCallExpression
import ast.FunctionDeclaration
import ast.IdentifierExpression
import ast.UnresolvedIdentifierExpression
import parser.CompositeToken
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.andRule
import parser.component6
import parser.listRule
import parser.optionalRule
import parser.validateGrammar
import parser.zeroOrMoreRule

// argumentsDeclaration -> identifier ( ","  identifier )
val argumentsDeclarationRule: Rule = listRule<UnresolvedIdentifierExpression>(identifierRule)

// in case of default block statement throws NPE
// hack to overcome circular dependency
val intermediateBlockStatementRule: Rule = Rule { ctx ->
    blockStatementRule.match(ctx)
}

// functionDeclaration -> "fun" identifier "(" argumentsDeclaration? ")" blockStatement
val functionDeclarationRule: Rule =
    andRule(
        funKeywordRule, identifierRule,
        leftParenRule, optionalRule(argumentsDeclarationRule), rightParenRule,
        intermediateBlockStatementRule
    ) { tokens ->
        val (_, functionNameToken, _, optionalArgumentsDeclarationToken, _, bodyToken) = tokens
        validateGrammar(
            functionNameToken is NodeToken
                    && functionNameToken.node is UnresolvedIdentifierExpression
        )
        validateGrammar(optionalArgumentsDeclarationToken is OptionalToken)
        validateGrammar(bodyToken is NodeToken && bodyToken.node is BlockStatement)

        val argNames = optionalArgumentsDeclarationToken
            .asExpressionList<IdentifierExpression>()
            .map { it.name }

        FunctionDeclaration(functionNameToken.node.name, argNames, bodyToken.node)
            .let(::NodeToken)
    }

// arguments -> expression ( "," expression )*
private val argumentsRule: Rule = listRule<Expression>(expressionRule)

// call -> primary ( "(" arguments? ")" )*
val callRule: Rule =
    andRule(
        primaryExpressionRule,
        zeroOrMoreRule(andRule(leftParenRule, optionalRule(argumentsRule), rightParenRule))
    ) combiner@{ tokens ->
        val (functionToken, callsToken) = tokens
        validateGrammar(functionToken is NodeToken && functionToken.node is Expression)
        validateGrammar(callsToken is CompositeToken)

        var processed: Expression = functionToken.node

        callsToken.tokens
            .chunked(3)
            .forEach { call ->
                val (_, optionalArgumentsToken, _) = call
                validateGrammar(optionalArgumentsToken is OptionalToken)
                processed = FunctionCallExpression(processed, optionalArgumentsToken.asExpressionList())
            }

        NodeToken(processed)
    }

private inline fun <reified T : Expression> OptionalToken.asExpressionList(): List<T> {
    if (token == null) return listOf()

    validateGrammar(token is CompositeToken)
    return token.tokens.map { argumentToken ->
        validateGrammar(argumentToken is NodeToken && argumentToken.node is T)
        argumentToken.node
    }
}
