package parser.rules

import ast.BlockStatement
import ast.CallExpression
import ast.Expression
import ast.FieldAccessExpression
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
import parser.orRule
import parser.throwInvalidGrammar
import parser.validateGrammar
import parser.zeroOrMoreRule

// argumentsDeclaration -> identifier ( ","  identifier )*
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

// call -> primary ( "(" arguments? ")" | "." identifier )*
val callRule: Rule =
    andRule(
        primaryExpressionRule,
        zeroOrMoreRule(
            orRule(
                andRule(leftParenRule, optionalRule(argumentsRule), rightParenRule) { it[1] },
                andRule(dotRule, identifierRule) { it[1] }
            )
        )
    ) combiner@{ tokens ->
        val (calleeToken, callsToken) = tokens
        validateGrammar(calleeToken is NodeToken && calleeToken.node is Expression)
        validateGrammar(callsToken is CompositeToken)

        var processed: Expression = calleeToken.node

        for (call in callsToken.tokens) {
            processed = when (call) {
                is OptionalToken -> CallExpression(processed, call.asExpressionList())
                is NodeToken -> {
                    validateGrammar(call.node is IdentifierExpression)
                    FieldAccessExpression(processed, call.node.name)
                }
                else -> throwInvalidGrammar()
            }
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
