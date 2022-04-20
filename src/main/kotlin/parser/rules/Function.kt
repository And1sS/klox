package parser.rules

import parser.CompositeToken
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.andRule
import parser.ast.BlockStatement
import parser.ast.Expression
import parser.ast.FunctionCallExpression
import parser.ast.FunctionValue
import parser.ast.IdentifierExpression
import parser.ast.VarDeclaration
import parser.component6
import parser.listRule
import parser.optionalRule
import parser.validateGrammar
import parser.zeroOrMoreRule

// argumentsDeclaration -> identifier ( ","  identifier )
val argumentsDeclarationRule: Rule = listRule<IdentifierExpression>(identifierRule)

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
        validateGrammar(functionNameToken is NodeToken && functionNameToken.node is IdentifierExpression)
        validateGrammar(optionalArgumentsDeclarationToken is OptionalToken)
        validateGrammar(bodyToken is NodeToken && bodyToken.node is BlockStatement)

        val function = FunctionValue(optionalArgumentsDeclarationToken.asExpressionList(), bodyToken.node)
        VarDeclaration(functionNameToken.node, function)
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
