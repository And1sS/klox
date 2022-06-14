package interpreter.astTraversals.runtime

import ast.BinaryOperatorExpression
import ast.OperatorType
import exception.EvaluationException
import interpreter.BooleanValue
import interpreter.Environment
import interpreter.NumericValue
import interpreter.StringValue
import interpreter.Value
import parser.validateGrammar
import parser.validateRuntimeBoolean
import kotlin.reflect.KClass

fun evaluateBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    val operatorType = expression.operatorType
    if (operatorType in setOf(OperatorType.And, OperatorType.Or)) {
        return evaluateLogicalBinaryOperatorExpression(expression, evaluationEnvironment)
    }

    val lhs = evaluateExpression(expression.lhs, evaluationEnvironment)
    val rhs = evaluateExpression(expression.rhs, evaluationEnvironment)

    val signature = BinaryOperatorSignature(operatorType, lhs::class, rhs::class)
    return binaryOperatorEvaluators[signature]?.invoke(lhs, rhs)
        ?: throw EvaluationException(
            "Invalid evaluation: could not find binary operator "
                    + "$operatorType for ${lhs::class} and ${rhs::class}"
        )
}

private fun evaluateLogicalBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    val lhsResult = evaluateExpression(expression.lhs, evaluationEnvironment)
    validateRuntimeBoolean(lhsResult)

    return if (
        (expression.operatorType == OperatorType.Or && lhsResult.value)
        || (expression.operatorType == OperatorType.And && !lhsResult.value)
    ) {
        lhsResult
    } else {
        val rhsResult = evaluateExpression(expression.rhs, evaluationEnvironment)
        validateRuntimeBoolean(rhsResult)
        rhsResult
    }
}

private typealias BinaryOperatorEvaluator = (Value, Value) -> Value
private typealias BinaryOperatorSignature =
        Triple<OperatorType, KClass<out Value>, KClass<out Value>>

private inline fun <reified L : Value, reified R : Value> binaryOperatorEvaluator(
    crossinline evaluator: (L, R) -> Value
): BinaryOperatorEvaluator = { lhs, rhs ->
    validateGrammar(lhs is L && rhs is R)

    evaluator(lhs, rhs)
}

private inline fun <reified T : Value> sameTypeBinaryOperatorEvaluator(
    type: OperatorType,
    crossinline evaluator: (T, T) -> Value
): Pair<BinaryOperatorSignature, BinaryOperatorEvaluator> =
    BinaryOperatorSignature(type, T::class, T::class) to binaryOperatorEvaluator(evaluator)

private inline fun numericalBinaryOperatorEvaluator(
    type: OperatorType,
    crossinline evaluator: (NumericValue, NumericValue) -> Value
) = sameTypeBinaryOperatorEvaluator(type, evaluator)

private val binaryOperatorEvaluators: Map<BinaryOperatorSignature, BinaryOperatorEvaluator> =
    mapOf(
        numericalBinaryOperatorEvaluator(OperatorType.Plus, ::add),
        numericalBinaryOperatorEvaluator(OperatorType.Minus, ::subtract),
        numericalBinaryOperatorEvaluator(OperatorType.Star, ::multiply),
        numericalBinaryOperatorEvaluator(OperatorType.Slash, ::divide),
        numericalBinaryOperatorEvaluator(OperatorType.Less, ::compareLess),
        numericalBinaryOperatorEvaluator(OperatorType.LessEqual, ::compareLessEqual),
        numericalBinaryOperatorEvaluator(OperatorType.Greater, ::compareGreater),
        numericalBinaryOperatorEvaluator(OperatorType.GreaterEqual, ::compareGreaterEqual),
        numericalBinaryOperatorEvaluator(OperatorType.EqualEqual, ::compareEqual),
        numericalBinaryOperatorEvaluator(OperatorType.BangEqual, ::compareNotEqual),
        sameTypeBinaryOperatorEvaluator(OperatorType.Plus, ::concatenate)
    )

private fun add(lhs: NumericValue, rhs: NumericValue): Value =
    NumericValue(lhs.value + rhs.value)

private fun concatenate(lhs: StringValue, rhs: StringValue): Value =
    StringValue(lhs.value + rhs.value)

private fun subtract(lhs: NumericValue, rhs: NumericValue): Value =
    NumericValue(lhs.value - rhs.value)

private fun multiply(lhs: NumericValue, rhs: NumericValue): Value =
    NumericValue(lhs.value * rhs.value)

private fun divide(lhs: NumericValue, rhs: NumericValue): Value =
    NumericValue(lhs.value / rhs.value)

private fun compareLess(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value < rhs.value)

private fun compareLessEqual(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value <= rhs.value)

private fun compareGreater(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value > rhs.value)

private fun compareGreaterEqual(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value >= rhs.value)

private fun compareEqual(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value == rhs.value)

private fun compareNotEqual(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value != rhs.value)
