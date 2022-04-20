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
import parser.ast.NilValue
import parser.ast.PrintStatement
import parser.ast.ReturnStatement
import parser.ast.Statement
import parser.ast.WhileStatement
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

// returnStatement -> return expression? ";"
val returnStatementRule: Rule =
    andRule(returnKeywordRule, optionalRule(expressionRule), semicolonRule) { tokens ->
        val (_, optionalExpressionToken, _) = tokens
        validateGrammar(optionalExpressionToken is OptionalToken)

        val returnResult: Expression = optionalExpressionToken.token?.let { expressionToken ->
            validateGrammar(expressionToken is NodeToken && expressionToken.node is Expression)
            expressionToken.node
        } ?: NilValue
        NodeToken(ReturnStatement(returnResult))
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

// whileStatement -> "while" "(" expression ")" statement
val whileStatementRule: Rule = andRule(
    whileKeywordRule, leftParenRule, expressionRule, rightParenRule,
    intermediateStatementRule
) { tokens ->
    val (_, _, conditionToken, _, bodyToken) = tokens
    validateGrammar(conditionToken is NodeToken && conditionToken.node is Expression)
    validateGrammar(bodyToken is NodeToken && bodyToken.node is Statement)

    NodeToken(WhileStatement(conditionToken.node, bodyToken.node))
}

// statement -> expressionStatement | printStatement | blockStatement
//                  | ifStatement | whileStatement | forStatement | returnStatement
val statementRule: Rule = orRule(
    expressionStatementRule,
    printStatementRule,
    blockStatementRule,
    ifStatementRule,
    whileStatementRule,
    forStatementRule,
    returnStatementRule
)
