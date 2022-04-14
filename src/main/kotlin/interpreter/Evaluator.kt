package interpreter

import parser.asString
import parser.ast.AssignmentExpression
import parser.ast.BinaryOperatorExpression
import parser.ast.BlockStatement
import parser.ast.BooleanValue
import parser.ast.Declaration
import parser.ast.Expression
import parser.ast.ExpressionStatement
import parser.ast.ForStatement
import parser.ast.IdentifierExpression
import parser.ast.IfStatement
import parser.ast.NilValue
import parser.ast.PrintStatement
import parser.ast.Statement
import parser.ast.UnaryOperatorExpression
import parser.ast.Value
import parser.ast.VarDeclaration
import parser.ast.WhileStatement
import parser.validateBoolean

fun executeDeclaration(declaration: Declaration, evaluationEnvironment: Environment) {
    when (declaration) {
        is VarDeclaration -> executeVarDeclaration(declaration, evaluationEnvironment)
        is Statement -> executeStatement(declaration, evaluationEnvironment)
    }
}

private fun executeVarDeclaration(declaration: VarDeclaration, evaluationEnvironment: Environment) {
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
        is IfStatement -> executeIfStatement(statement, evaluationEnvironment)
        is WhileStatement -> executeWhileStatement(statement, evaluationEnvironment)
        is ForStatement -> executeForStatement(statement, evaluationEnvironment)
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
        executeDeclaration(declaration, blockEnvironment)
    }
}

private fun executeIfStatement(statement: IfStatement, evaluationEnvironment: Environment) {
    val conditionValue = evaluateExpression(statement.condition, evaluationEnvironment)
    validateBoolean(conditionValue)

    val body = if (conditionValue.value) statement.body else statement.elseBody
    body?.let { executeStatement(it, evaluationEnvironment) }
}

private fun executeWhileStatement(statement: WhileStatement, evaluationEnvironment: Environment) {
    while (true) {
        val condition = evaluateExpression(statement.condition, evaluationEnvironment)
        validateBoolean(condition)

        if (!condition.value) break
        executeStatement(statement.body, evaluationEnvironment)
    }
}

private fun executeForStatement(statement: ForStatement, evaluationEnvironment: Environment) {
    val forEnvironment = Environment(evaluationEnvironment)
    statement.initializer?.let { executeDeclaration(it, forEnvironment) }

    while (true) {
        val condition: Value = statement.condition?.let {
            evaluateExpression(it, forEnvironment)
        } ?: BooleanValue(true)

        validateBoolean(condition)
        if (!condition.value) break

        executeStatement(statement.body, forEnvironment)
        statement.increment?.let { evaluateExpression(it, forEnvironment) }
    }
}

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Value -> expr
    is IdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is AssignmentExpression -> evaluateAssignmentExpression(expr, evaluationEnvironment)
}

private fun evaluateAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): Value {
    val exprValue = evaluateExpression(expr.expr, evaluationEnvironment)
    evaluationEnvironment.assignVariable(expr.identifier, exprValue)
    return exprValue
}
