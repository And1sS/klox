package interpreter

import parser.ast.AssignmentExpression
import parser.ast.BinaryOperatorExpression
import parser.ast.Expression
import parser.ast.FunctionCallExpression
import parser.ast.FunctionValue
import parser.ast.IdentifierExpression
import parser.ast.NilValue
import parser.ast.UnaryOperatorExpression
import parser.ast.Value
import parser.validateRuntime

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Value -> expr
    is IdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is AssignmentExpression -> evaluateAssignmentExpression(expr, evaluationEnvironment)
    is FunctionCallExpression -> evaluateFunctionCallExpression(expr, evaluationEnvironment)
}

private fun evaluateFunctionCallExpression(
    expr: FunctionCallExpression,
    evaluationEnvironment: Environment
): Value {
    val functionValue = evaluateExpression(expr.function, evaluationEnvironment)
    validateRuntime(functionValue is FunctionValue) {
        "Cannot call expression of type ${functionValue::class}"
    }

    validateRuntime(functionValue.argNumber == expr.arguments.size) {
        "Expected ${functionValue.argNumber} arguments, but got ${expr.arguments.size}"
    }

    val functionEnvironment = Environment(evaluationEnvironment)
    expr.arguments
        .map { arg -> evaluateExpression(arg, evaluationEnvironment) }
        .let(functionValue.argNames::zip)
        .forEach { (argName, argValue) -> functionEnvironment.createVariable(argName, argValue) }

    return when (val result = executeBlockStatement(functionValue.body, functionEnvironment)) {
        is Nothing -> NilValue
        is Return -> result.value
    }
}

private fun evaluateAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): Value = evaluateExpression(expr.expr, evaluationEnvironment).also {
    evaluationEnvironment.assignVariable(expr.identifier, it)
}
