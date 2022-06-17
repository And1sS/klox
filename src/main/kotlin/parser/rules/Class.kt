package parser.rules

import ast.ClassDeclaration
import ast.Declaration
import ast.FunctionDeclaration
import ast.IdentifierExpression
import ast.VarDeclaration
import parser.CompositeToken
import parser.NodeToken
import parser.Rule
import parser.andRule
import parser.orRule
import parser.validateGrammar
import parser.zeroOrMoreRule

// classContents -> (varDeclaration | functionDeclaration)*
val classContentsRule: Rule = zeroOrMoreRule(
    orRule(varKeywordRule, functionDeclarationRule)
)

// classDeclaration -> "class" identifier "{" classContents "}"
val classDeclarationRule: Rule = andRule(
    classKeywordRule,
    identifierRule,
    leftBraceRule,
    classContentsRule,
    rightBraceRule
) { tokens ->
    val (_, nameToken, _, membersToken, _) = tokens
    validateGrammar(nameToken is NodeToken && nameToken.node is IdentifierExpression)
    validateGrammar(membersToken is CompositeToken)

    val members = membersToken.tokens.map { token ->
        validateGrammar(token is NodeToken)
        validateGrammar(token.node is VarDeclaration || token.node is FunctionDeclaration)
        token.node as Declaration
    }

    NodeToken(ClassDeclaration(nameToken.node.name, members))
}