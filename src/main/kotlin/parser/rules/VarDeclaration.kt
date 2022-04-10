package parser.rules

import parser.Combiner
import parser.ast.Expression
import parser.ast.IdentifierExpression
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.ast.VarDeclaration
import parser.andRule
import parser.optionalRule

// varDeclaration -> "var" identifier ( "=" expression )? ";"
val varDeclarationRule: Rule = andRule(
    varRule,
    identifierRule,
    optionalRule(
        andRule(equalRule, expressionRule) { assignmentCombiner(it) }
    ),
    semicolonRule,
) { varDeclarationCombiner(it) }

private val varDeclarationCombiner: Combiner = { tokens ->
    val (_, identifierToken, optionalToken, _) = tokens
    require(identifierToken is NodeToken && identifierToken.node is IdentifierExpression) {
        "Invalid grammar"
    }
    require(optionalToken is OptionalToken) {
        "Invalid grammar"
    }

    val valueExpression: Expression? = optionalToken.token?.let {
        require(optionalToken.token is NodeToken && optionalToken.token.node is Expression) {
            "Invalid grammar"
        }

        optionalToken.token.node
    }

    NodeToken(VarDeclaration(identifierToken.node, valueExpression))
}

private val assignmentCombiner: Combiner = { tokens ->
    val (_, expressionToken) = tokens
    require(expressionToken is NodeToken && expressionToken.node is Expression) {
        "Invalid grammar"
    }

    NodeToken(expressionToken.node)
}