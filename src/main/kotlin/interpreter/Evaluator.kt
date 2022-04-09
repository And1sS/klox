package interpreter

import parser.BinaryOperatorExpression
import parser.Declaration
import parser.Expression
import parser.ExpressionStatement
import parser.IdentifierExpression
import parser.NilValue
import parser.PrintStatement
import parser.Statement
import parser.UnaryOperatorExpression
import parser.Value
import parser.VarDeclaration
import parser.asString

fun evaluateDeclaration(declaration: Declaration, evaluationEnvironment: Environment) {
    when (declaration) {
        is VarDeclaration -> evaluateVarDeclaration(declaration, evaluationEnvironment)
        is Statement -> evaluateStatement(declaration, evaluationEnvironment)
    }
}

private fun evaluateVarDeclaration(declaration: VarDeclaration, evaluationEnvironment: Environment) {
    val variableValue = declaration.value?.let {
        evaluateExpression(it, evaluationEnvironment)
    } ?: NilValue

    evaluationEnvironment.createVariable(declaration.identifier, variableValue)
}

private fun evaluateStatement(statement: Statement, evaluationEnvironment: Environment) {
    when (statement) {
        is ExpressionStatement -> evaluateExpression(statement.expr, evaluationEnvironment)
        is PrintStatement ->
            evaluateExpression(statement.expr, evaluationEnvironment)
                .asString()
                .let(::println)
    }
}

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Value -> expr
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is IdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
}
