package interpreter.astTraversals.runtime

import ast.AssignmentExpression
import ast.BinaryOperatorExpression
import ast.BooleanLiteral
import ast.Expression
import ast.FunctionCallExpression
import ast.Literal
import ast.NilLiteral
import ast.NumericLiteral
import ast.ResolvedIdentifierExpression
import ast.StringLiteral
import ast.UnaryOperatorExpression
import ast.UnresolvedIdentifierExpression
import exception.EvaluationException
import interpreter.BooleanValue
import interpreter.Environment
import interpreter.FunctionValue
import interpreter.LoxFunctionValue
import interpreter.NativeFunctionValue
import interpreter.NilValue
import interpreter.NumericValue
import interpreter.StringValue
import interpreter.Value
import parser.validateRuntime

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Literal -> evaluateLiteral(expr)
    is ResolvedIdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is AssignmentExpression -> evaluateAssignmentExpression(expr, evaluationEnvironment)
    is FunctionCallExpression -> evaluateFunctionCallExpression(expr, evaluationEnvironment)
    // this branch shouldn't have been reached
    is UnresolvedIdentifierExpression ->
        throw EvaluationException("Trying to evaluate unresolved variable")
}

private fun evaluateLiteral(expr: Literal): Value = when (expr) {
    is NilLiteral -> NilValue
    is BooleanLiteral -> BooleanValue(expr.value)
    is NumericLiteral -> NumericValue(expr.value)
    is StringLiteral -> StringValue(expr.value)
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
        is LoxFunctionValue -> callLoxFunction(functionValue, argumentValues)
        is NativeFunctionValue -> functionValue.call(argumentValues)
    }
}

private fun callLoxFunction(
    functionValue: LoxFunctionValue,
    argumentValues: List<Value>
): Value {
    val functionEnvironment = Environment(functionValue.capturingEnvironment)

    argumentValues.let(functionValue.argNames::zip)
        .forEach { (argName, argValue) -> functionEnvironment.createVariable(argName, argValue) }

    return executeBlockStatement(functionValue.body, functionEnvironment).unwrap()
}

private fun evaluateAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): Value = evaluateExpression(expr.expr, evaluationEnvironment).also {
    require(expr.identifier is ResolvedIdentifierExpression) {
        "Unresolved identifier encountered in assignment: ${expr.identifier}"
    }
    evaluationEnvironment.assignVariable(expr.identifier, it)
}
