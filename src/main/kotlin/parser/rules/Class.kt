package parser.rules

import ast.ClassDeclaration
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
    orRule(varDeclarationRule, functionDeclarationRule)
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

    val className = nameToken.node.name
    val fields = mutableListOf<VarDeclaration>()
    val methods = mutableListOf<FunctionDeclaration>()
    val constructors = mutableListOf<FunctionDeclaration>()

    for (token in membersToken.tokens) {
        validateGrammar(token is NodeToken)
        val member = token.node
        when {
            member is VarDeclaration && member.name == className ->
                throw IllegalArgumentException(
                    "Can't declare field ${member.name} for class $className"
                )
            member is VarDeclaration -> fields.add(member)

            member is FunctionDeclaration && member.name == className -> constructors.add(member)
            member is FunctionDeclaration -> methods.add(member)

            else -> throw IllegalArgumentException("Illegal grammar")
            // TODO: throw meaningful exception,
            //  consider refactoring require() usages because they throw IllegalArgumentException
        }
    }

    NodeToken(ClassDeclaration(className, fields, methods, constructors))
}