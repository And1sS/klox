package interpreter

import parser.asString
import parser.ast.BlockStatement
import parser.ast.BooleanValue
import parser.ast.Declaration
import parser.ast.ExpressionStatement
import parser.ast.ForStatement
import parser.ast.IfStatement
import parser.ast.NilValue
import parser.ast.PrintStatement
import parser.ast.ReturnStatement
import parser.ast.Statement
import parser.ast.Value
import parser.ast.VarDeclaration
import parser.ast.WhileStatement
import parser.validateRuntimeBoolean

// TODO: think of wrapping inner classes
// ExecutionResult.Nothing vs just Nothing
sealed class ExecutionResult
object Nothing : ExecutionResult()
data class Return(val value: Value) : ExecutionResult()

fun executeDeclaration(
    declaration: Declaration,
    evaluationEnvironment: Environment
): ExecutionResult = when (declaration) {
    is VarDeclaration -> executeVarDeclaration(declaration, evaluationEnvironment)
    is Statement -> executeStatement(declaration, evaluationEnvironment)
}

private fun executeVarDeclaration(
    declaration: VarDeclaration,
    evaluationEnvironment: Environment
): ExecutionResult {
    val variableValue = declaration.value?.let {
        evaluateExpression(it, evaluationEnvironment)
    } ?: NilValue

    evaluationEnvironment.createVariable(declaration.identifier, variableValue)

    return Nothing
}

private fun executeStatement(
    statement: Statement,
    evaluationEnvironment: Environment
): ExecutionResult = when (statement) {
    is ExpressionStatement -> evaluateExpression(statement.expr, evaluationEnvironment).let { Nothing }
    is PrintStatement -> executePrintStatement(statement, evaluationEnvironment)
    is BlockStatement -> executeBlockStatement(statement, evaluationEnvironment)
    is IfStatement -> executeIfStatement(statement, evaluationEnvironment)
    is WhileStatement -> executeWhileStatement(statement, evaluationEnvironment)
    is ForStatement -> executeForStatement(statement, evaluationEnvironment)
    is ReturnStatement -> executeReturnStatement(statement, evaluationEnvironment)
}

private fun executePrintStatement(
    statement: PrintStatement,
    evaluationEnvironment: Environment
): ExecutionResult =
    evaluateExpression(statement.expr, evaluationEnvironment)
        .asString()
        .also(::println)
        .let { Nothing }

fun executeBlockStatement(
    statement: BlockStatement,
    evaluationEnvironment: Environment
): ExecutionResult {
    val blockEnvironment = Environment(evaluationEnvironment)
    for (declaration in statement.declarations) {
        val result = executeDeclaration(declaration, blockEnvironment)
        if (result is Return)
            return result
    }
    return Nothing
}

private fun executeIfStatement(
    statement: IfStatement,
    evaluationEnvironment: Environment
): ExecutionResult {
    val conditionValue = evaluateExpression(statement.condition, evaluationEnvironment)
    validateRuntimeBoolean(conditionValue)

    val body = if (conditionValue.value) statement.body else statement.elseBody
    return body?.let { executeStatement(it, evaluationEnvironment) } ?: Nothing
}

private fun executeWhileStatement(
    statement: WhileStatement,
    evaluationEnvironment: Environment
): ExecutionResult {
    while (true) {
        val condition = evaluateExpression(statement.condition, evaluationEnvironment)
        validateRuntimeBoolean(condition)

        if (!condition.value) break
        val result = executeStatement(statement.body, evaluationEnvironment)
        if (result is Return)
            return result
    }
    return Nothing
}

private fun executeForStatement(
    statement: ForStatement,
    evaluationEnvironment: Environment
): ExecutionResult {
    val forEnvironment = Environment(evaluationEnvironment)
    statement.initializer?.let { executeDeclaration(it, forEnvironment) }

    while (true) {
        val condition: Value = statement.condition?.let {
            evaluateExpression(it, forEnvironment)
        } ?: BooleanValue(true)

        validateRuntimeBoolean(condition)
        if (!condition.value) break

        val result = executeStatement(statement.body, forEnvironment)
        if (result is Return)
            return result
        statement.increment?.let { evaluateExpression(it, forEnvironment) }
    }

    return Nothing
}

private fun executeReturnStatement(
    statement: ReturnStatement,
    evaluationEnvironment: Environment
): ExecutionResult =
    evaluateExpression(statement.expr, evaluationEnvironment)
        .let(::Return)