package interpreter

import ast.AssignmentExpression
import ast.BinaryOperatorExpression
import ast.Expression
import ast.FunctionCallExpression
import ast.FunctionValue
import ast.IdentifierExpression
import ast.LoxFunctionValue
import ast.NativeFunctionValue
import ast.NilValue
import ast.UnaryOperatorExpression
import ast.Value
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

    val argumentValues: List<Value> = expr.arguments
        .map { arg -> evaluateExpression(arg, evaluationEnvironment) }

    return when (functionValue) {
        is LoxFunctionValue -> callLoxFunction(functionValue, argumentValues, evaluationEnvironment)
        is NativeFunctionValue -> functionValue.call(argumentValues)
    }
}

private fun callLoxFunction(
    functionValue: LoxFunctionValue,
    argumentValues: List<Value>,
    evaluationEnvironment: Environment
): Value {
    val functionEnvironment = Environment(evaluationEnvironment)

    argumentValues.let(functionValue.argNames::zip)
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
