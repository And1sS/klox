package interpreter.astTraversals.runtime

import ast.AssignmentExpression
import ast.BinaryOperatorExpression
import ast.BooleanLiteral
import ast.CallExpression
import ast.Expression
import ast.FieldAccessExpression
import ast.LabelExpression
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
import interpreter.ObjectValue
import interpreter.StringValue
import interpreter.Value
import parser.validateRuntime

fun evaluateExpression(expr: Expression, evaluationEnvironment: Environment): Value = when (expr) {
    is Literal -> evaluateLiteral(expr)
    is LabelExpression -> evaluateLabelExpression(expr, evaluationEnvironment)
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(expr, evaluationEnvironment)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(expr, evaluationEnvironment)
    is AssignmentExpression -> evaluateAssignmentExpression(expr, evaluationEnvironment)
    is CallExpression -> evaluateCallExpression(expr, evaluationEnvironment)
}

private fun evaluateLiteral(expr: Literal): Value = when (expr) {
    is NilLiteral -> NilValue
    is BooleanLiteral -> BooleanValue(expr.value)
    is NumericLiteral -> NumericValue(expr.value)
    is StringLiteral -> StringValue(expr.value)
}

private fun evaluateCallExpression(
    expr: CallExpression,
    evaluationEnvironment: Environment
): Value {
    val argumentValues: List<Value> = expr.arguments
        .map { arg -> evaluateExpression(arg, evaluationEnvironment) }

    return when (val callableValue = evaluateExpression(expr.function, evaluationEnvironment)) {
        is FunctionValue -> evaluateFunctionCall(callableValue, argumentValues)
        is interpreter.ClassValue -> evaluateConstructorCall(
            callableValue,
            argumentValues
        )
        else -> throw EvaluationException(
            "Cannot call expression of type ${callableValue::class}"
        )
    }
}

private fun evaluateFunctionCall(
    function: FunctionValue,
    arguments: List<Value>
): Value {
    validateRuntime(function.argNumber == arguments.size) {
        "Expected ${function.argNumber} arguments, but got ${arguments.size}"
    }
    return when (function) {
        is LoxFunctionValue -> evaluateLoxFunctionCall(function, arguments)
        is NativeFunctionValue -> function.call(arguments)
    }
}

private fun evaluateConstructorCall(
    klass: interpreter.ClassValue,
    arguments: List<Value>
): Value {
    val objectEnvironment = Environment(klass.capturedEnvironment)
    val objectValue = ObjectValue(klass, objectEnvironment)

    for (field in klass.fields) {
        // declare all fields with NilValue
        // in case any field is referenced before initialization
        objectEnvironment.createVariable(field.name, NilValue)
    }

    for (method in klass.methods) {
        executeDeclaration(method, objectEnvironment)
    }

    objectEnvironment.createVariable("this", objectValue)

    for (field in klass.fields) {
        objectEnvironment.assignVariable(
            ResolvedIdentifierExpression(field.name, 0),
            field.initializationExpression?.let {
                evaluateExpression(it, objectEnvironment)
            } ?: NilValue
        )
    }

    val boundConstructor = LoxFunctionValue(
        klass.constructor.argNames,
        klass.constructor.body,
        objectEnvironment
    )
    evaluateFunctionCall(boundConstructor, arguments)

    return objectValue
}

private fun evaluateLoxFunctionCall(
    functionValue: LoxFunctionValue,
    argumentValues: List<Value>
): Value {
    val functionEnvironment = Environment(functionValue.capturedEnvironment)

    argumentValues.let(functionValue.argNames::zip)
        .forEach { (argName, argValue) -> functionEnvironment.createVariable(argName, argValue) }

    return executeBlockStatement(functionValue.body, functionEnvironment).unwrap()
}

private fun evaluateLabelExpression(
    expr: LabelExpression,
    evaluationEnvironment: Environment
): Value = when (expr) {
    is ResolvedIdentifierExpression -> evaluationEnvironment.getVariableValue(expr)
    is FieldAccessExpression -> evaluateFieldAccessExpression(expr, evaluationEnvironment)
    // this branch shouldn't be reached
    is UnresolvedIdentifierExpression -> throw EvaluationException(
        "Unresolved identifier encountered in assignment: ${expr.name}"
    )
}

private fun evaluateAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): Value {
    val result = evaluateExpression(expr.expr, evaluationEnvironment)
    when (expr.label) {
        is ResolvedIdentifierExpression -> evaluationEnvironment.assignVariable(expr.label, result)
        is FieldAccessExpression -> assignField(expr.label, result, evaluationEnvironment)
        // this branch shouldn't be reached
        is UnresolvedIdentifierExpression -> throw EvaluationException(
            "Unresolved identifier encountered in assignment: ${expr.label.name}"
        )
    }
    return result
}

private fun assignField(
    expr: FieldAccessExpression,
    value: Value,
    evaluationEnvironment: Environment
): Unit = accessField(expr, evaluationEnvironment) {
    assignVariable(ResolvedIdentifierExpression(expr.memberName, 0), value)
}

private fun evaluateFieldAccessExpression(
    expr: FieldAccessExpression,
    evaluationEnvironment: Environment
): Value = accessField(expr, evaluationEnvironment) {
    getVariableValue(ResolvedIdentifierExpression(expr.memberName, 0))
}

private inline fun <reified T> accessField(
    expr: FieldAccessExpression,
    evaluationEnvironment: Environment,
    block: Environment.() -> T
): T {
    val objectValue = evaluateExpression(expr.lhs, evaluationEnvironment)
    val errorMessage = "Can't access field ${expr.memberName} of $objectValue"

    validateRuntime(objectValue is ObjectValue) { errorMessage }

    try {
        return objectValue.objectEnvironment.block()
    } catch (_: EvaluationException) {
        throw EvaluationException(errorMessage)
    }
}
