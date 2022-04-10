package parser.rules

import parser.CompositeToken
import parser.NodeToken
import parser.ParserToken
import parser.ProgramToken
import parser.Rule
import parser.andRule
import parser.ast.BlockStatement
import parser.ast.Declaration
import parser.orRule
import parser.zeroOrMoreRule

// hack to overcome circular dependency
private val intermediateDeclarationRule: Rule = Rule { ctx ->
    declarationRule.match(ctx)
}

// block -> "{" declaration* "}"
val blockRule: Rule =
    andRule(leftBraceRule, zeroOrMoreRule(intermediateDeclarationRule), rightBraceRule) { tokens ->
        val (_, declarationsToken, _) = tokens
        require(declarationsToken is CompositeToken) { "Invalid grammar" }

        declarationsToken.tokens
            .asDeclarationList()
            .let(::BlockStatement)
            .let(::NodeToken)
    }

// declaration -> varDeclaration | statement | block
val declarationRule: Rule = orRule(varDeclarationRule, statementRule, blockRule)

// program -> declaration*
val programRule: Rule = zeroOrMoreRule(declarationRule) { tokens ->
    tokens
        .asDeclarationList()
        .let(::ProgramToken)
}

private fun List<ParserToken>.asDeclarationList(): List<Declaration> =
    map { token ->
        require(token is NodeToken && token.node is Declaration) { "Invalid grammar" }
        token.node
    }