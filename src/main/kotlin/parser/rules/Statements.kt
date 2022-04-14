package parser.rules

import parser.CompositeToken
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.andRule
import parser.ast.BlockStatement
import parser.ast.Expression
import parser.ast.ExpressionStatement
import parser.ast.IfStatement
import parser.ast.PrintStatement
import parser.ast.Statement
import parser.component6
import parser.optionalRule
import parser.orRule
import parser.validateGrammar
import parser.zeroOrMoreRule

// printStatement -> "print" expression ";"
val printStatementRule: Rule = andRule(printKeywordRule, expressionRule, semicolonRule) { tokens ->
    val (_, expressionToken, _) = tokens
    validateGrammar(expressionToken is NodeToken && expressionToken.node is Expression)

    NodeToken(PrintStatement(expressionToken.node))
}

// expressionStatement -> expression ";"
val expressionStatementRule: Rule = andRule(expressionRule, semicolonRule) { tokens ->
    val (expressionToken, _) = tokens
    validateGrammar(expressionToken is NodeToken && expressionToken.node is Expression)

    NodeToken(ExpressionStatement(expressionToken.node))
}

// blockStatement -> "{" declaration* "}"
val blockStatementRule: Rule =
    andRule(leftBraceRule, zeroOrMoreRule(intermediateDeclarationRule), rightBraceRule) { tokens ->
        val (_, declarationsToken, _) = tokens
        validateGrammar(declarationsToken is CompositeToken)

        declarationsToken.tokens
            .asDeclarationList()
            .let(::BlockStatement)
            .let(::NodeToken)
    }

// hack to overcome circular dependency
val intermediateStatementRule: Rule = Rule { ctx -> statementRule.match(ctx) }

// ifStatement -> "if" "(" expression ")" statement ( "else" statement )?
val ifStatementRule: Rule = andRule(
    ifKeywordRule, leftParenRule, expressionRule, rightParenRule,
    intermediateStatementRule,
    optionalRule(andRule(elseKeywordRule, intermediateStatementRule))
) { tokens ->
    val (_, _, conditionToken, _, bodyToken, optionalElseToken) = tokens
    validateGrammar(conditionToken is NodeToken && conditionToken.node is Expression)
    validateGrammar(bodyToken is NodeToken && bodyToken.node is Statement)
    validateGrammar(optionalElseToken is OptionalToken)

    val elseBody: Statement? = optionalElseToken.token?.let { elseToken ->
        validateGrammar(elseToken is CompositeToken)
        val (_, elseBodyToken) = elseToken.tokens
        validateGrammar(elseBodyToken is NodeToken && elseBodyToken.node is Statement)
        elseBodyToken.node
    }

   NodeToken(IfStatement(conditionToken.node, bodyToken.node, elseBody))
}

// statement -> expressionStatement | printStatement | blockStatement | ifStatement
val statementRule: Rule = orRule(
    expressionStatementRule,
    printStatementRule,
    blockStatementRule,
    ifStatementRule
)
