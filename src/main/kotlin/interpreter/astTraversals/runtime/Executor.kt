package interpreter.astTraversals.runtime

import parser.asString
import ast.BlockStatement
import ast.BooleanValue
import ast.Declaration
import ast.ExpressionStatement
import ast.ForStatement
import ast.FunctionDeclaration
import ast.IfStatement
import ast.LoxFunctionValue
import ast.NilValue
import ast.PrintStatement
import ast.ReturnStatement
import ast.Statement
import ast.Value
import ast.VarDeclaration
import ast.WhileStatement
import interpreter.Environment
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
    is FunctionDeclaration -> executeFunctionDeclaration(declaration, evaluationEnvironment)
    is Statement -> executeStatement(declaration, evaluationEnvironment)
}

private fun executeVarDeclaration(
    declaration: VarDeclaration,
    evaluationEnvironment: Environment
): ExecutionResult {
    val variableValue = declaration.initializationExpression?.let {
        evaluateExpression(it, evaluationEnvironment)
    } ?: NilValue

    evaluationEnvironment.createVariable(declaration.identifier, variableValue)

    return Nothing
}

private fun executeFunctionDeclaration(
    declaration: FunctionDeclaration,
    evaluationEnvironment: Environment
): ExecutionResult {
    val functionValue = LoxFunctionValue(declaration.argNames, declaration.body, evaluationEnvironment)

    evaluationEnvironment.createVariable(declaration.identifier, functionValue)

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
