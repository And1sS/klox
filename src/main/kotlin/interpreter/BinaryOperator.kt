package interpreter

import exception.EvaluationException
import parser.ast.BinaryOperatorExpression
import parser.ast.BooleanValue
import parser.ast.NumericValue
import parser.ast.OperatorType
import parser.ast.StringValue
import parser.ast.Value
import kotlin.reflect.KClass

fun evaluateBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    val operatorType = expression.operatorType
    val lhs = evaluateExpression(expression.lhs, evaluationEnvironment)
    val rhs = evaluateExpression(expression.rhs, evaluationEnvironment)

    val signature = BinaryOperatorSignature(operatorType, lhs::class, rhs::class)
    return binaryOperatorEvaluators[signature]?.invoke(lhs, rhs)
        ?: throw EvaluationException(
            "Invalid evaluation: could not find binary operator "
                    + "$operatorType for ${lhs::class} and ${rhs::class}"
        )
}

private typealias BinaryOperatorEvaluator = (Value, Value) -> Value
private typealias BinaryOperatorSignature =
        Triple<OperatorType, KClass<out Value>, KClass<out Value>>

private inline fun <reified L : Value, reified R : Value> binaryOperatorEvaluator(
    crossinline evaluator: (L, R) -> Value
): BinaryOperatorEvaluator = { lhs, rhs ->
    require(lhs is L && rhs is R) {
        "This branch shouldn't have been reached"
    }
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
