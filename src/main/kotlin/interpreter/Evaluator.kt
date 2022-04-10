package interpreter

import parser.asString
import parser.ast.BinaryOperatorExpression
import parser.ast.BlockStatement
import parser.ast.Declaration
import parser.ast.Expression
import parser.ast.ExpressionStatement
import parser.ast.IdentifierExpression
import parser.ast.NilValue
import parser.ast.PrintStatement
import parser.ast.Statement
import parser.ast.UnaryOperatorExpression
import parser.ast.Value
import parser.ast.VarDeclaration

fun evaluateDeclaration(declaration: Declaration, evaluationEnvironment: Environment) {
    when (declaration) {
        is VarDeclaration -> evaluateVarDeclaration(declaration, evaluationEnvironment)
        is Statement -> executeStatement(declaration, evaluationEnvironment)
    }
}

private fun evaluateVarDeclaration(declaration: VarDeclaration, evaluationEnvironment: Environment) {
    val variableValue = declaration.value?.let {
        evaluateExpression(it, evaluationEnvironment)
    } ?: NilValue

    evaluationEnvironment.createVariable(declaration.identifier, variableValue)
}

private fun executeStatement(statement: Statement, evaluationEnvironment: Environment) {
    when (statement) {
        is ExpressionStatement -> evaluateExpression(statement.expr, evaluationEnvironment)
        is PrintStatement -> executePrintStatement(statement, evaluationEnvironment)
        is BlockStatement -> executeBlockStatement(statement, evaluationEnvironment)
    }
}

private fun executePrintStatement(statement: PrintStatement, evaluationEnvironment: Environment) {
    evaluateExpression(statement.expr, evaluationEnvironment)
        .asString()
        .let(::println)
}

private fun executeBlockStatement(statement: BlockStatement, evaluationEnvironment: Environment) {
    val blockEnvironment = Environment(evaluationEnvironment)
    for (declaration in statement.declarations) {
        evaluateDeclaration(declaration, blockEnvironment)
    }
}

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Value -> expr
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is IdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
}
