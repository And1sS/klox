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
import parser.validateGrammar

// varDeclaration -> "var" identifier ( "=" expression )? ";"
val varDeclarationRule: Rule = andRule(
    varKeywordRule,
    identifierRule,
    optionalRule(
        andRule(equalRule, expressionRule) { assignmentCombiner(it) }
    ),
    semicolonRule,
) { varDeclarationCombiner(it) }

private val varDeclarationCombiner: Combiner = { tokens ->
    val (_, identifierToken, optionalToken, _) = tokens
    validateGrammar(identifierToken is NodeToken && identifierToken.node is IdentifierExpression)
    validateGrammar(optionalToken is OptionalToken)

    val valueExpression: Expression? = optionalToken.token?.let {
        validateGrammar(optionalToken.token is NodeToken && optionalToken.token.node is Expression)

        optionalToken.token.node
    }

    NodeToken(VarDeclaration(identifierToken.node, valueExpression))
}

private val assignmentCombiner: Combiner = { tokens ->
    val (_, expressionToken) = tokens
    validateGrammar(expressionToken is NodeToken && expressionToken.node is Expression)

    NodeToken(expressionToken.node)
}
