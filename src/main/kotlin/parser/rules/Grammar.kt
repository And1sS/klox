package parser.rules

import parser.NodeToken
import parser.ParserToken
import parser.ProgramToken
import parser.Rule
import ast.Declaration
import parser.orRule
import parser.validateGrammar
import parser.zeroOrMoreRule

// hack to overcome circular dependency
val intermediateDeclarationRule: Rule = Rule { ctx -> declarationRule.match(ctx) }

// varDeclaration is separated from statement to prohibit
// this case: if (monday) var beverage = "espresso";
// declaration ->  statement | varDeclaration | functionDeclaration
val declarationRule: Rule = orRule(statementRule, varDeclarationRule, functionDeclarationRule)

// program -> declaration*
val programRule: Rule = zeroOrMoreRule(declarationRule) { tokens ->
    tokens
        .asDeclarationList()
        .let(::ProgramToken)
}

fun List<ParserToken>.asDeclarationList(): List<Declaration> =
    map { token ->
        validateGrammar(token is NodeToken && token.node is Declaration)

        token.node
    }
