package interpreter.astTraversals.runtime

import ast.BinaryOperatorExpression
import ast.OperatorType
import exception.EvaluationException
import interpreter.BooleanValue
import interpreter.Environment
import interpreter.NilValue
import interpreter.NumericValue
import interpreter.ObjectValue
import interpreter.StringValue
import interpreter.Value
import parser.validateGrammar
import parser.validateRuntimeBoolean
import kotlin.reflect.KClass

fun evaluateBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    if (expression.operatorType in setOf(OperatorType.And, OperatorType.Or)) {
        return evaluateLogicalBinaryOperatorExpression(expression, evaluationEnvironment)
    }

    val lhs = evaluateExpression(expression.lhs, evaluationEnvironment)
    val rhs = evaluateExpression(expression.rhs, evaluationEnvironment)
    val signature = BinaryOperatorSignature(expression.operatorType, lhs::class, rhs::class)

    if (lhs is NilValue || rhs is NilValue) return evaluateBinaryOperatorOnNil(signature)

    return binaryOperatorEvaluators[signature]?.invoke(lhs, rhs)
        ?: throwAbsentBinaryOperatorException(signature)
}

private fun throwAbsentBinaryOperatorException(
    signature: BinaryOperatorSignature
): Nothing = throw EvaluationException(
    "Invalid evaluation: could not find binary operator "
            + "${signature.operatorType} for ${signature.lhsType} and ${signature.rhsType}"
)

private fun evaluateLogicalBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    val lhsResult = evaluateExpression(expression.lhs, evaluationEnvironment)
    validateRuntimeBoolean(lhsResult)

    val shortCircuit = (expression.operatorType == OperatorType.Or && lhsResult.value)
            || (expression.operatorType == OperatorType.And && !lhsResult.value)

    return if (shortCircuit) {
        lhsResult
    } else {
        evaluateExpression(expression.rhs, evaluationEnvironment)
            .also(::validateRuntimeBoolean)
    }
}

private fun evaluateBinaryOperatorOnNil(
    signature: BinaryOperatorSignature
): Value = when (signature.operatorType) {
    OperatorType.EqualEqual -> BooleanValue(signature.lhsType == signature.rhsType)
    OperatorType.BangEqual -> BooleanValue(signature.lhsType != signature.rhsType)
    else -> throwAbsentBinaryOperatorException(signature)
}

private typealias BinaryOperatorEvaluator = (Value, Value) -> Value

data class BinaryOperatorSignature(
    val operatorType: OperatorType,
    val lhsType: KClass<out Value>,
    val rhsType: KClass<out Value>
)

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
        sameTypeBinaryOperatorEvaluator(OperatorType.EqualEqual, ::compareEqualBoolean),
        sameTypeBinaryOperatorEvaluator(OperatorType.EqualEqual, ::compareEqualNumeric),
        sameTypeBinaryOperatorEvaluator(OperatorType.EqualEqual, ::compareEqualString),
        sameTypeBinaryOperatorEvaluator(OperatorType.EqualEqual, ::compareEqualObject),
        sameTypeBinaryOperatorEvaluator(OperatorType.BangEqual, ::compareNotEqualBoolean),
        sameTypeBinaryOperatorEvaluator(OperatorType.BangEqual, ::compareNotEqualNumeric),
        sameTypeBinaryOperatorEvaluator(OperatorType.BangEqual, ::compareNotEqualString),
        sameTypeBinaryOperatorEvaluator(OperatorType.BangEqual, ::compareNotEqualObject),
        sameTypeBinaryOperatorEvaluator(OperatorType.Plus, ::concatenate),
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

private fun compareEqualBoolean(lhs: BooleanValue, rhs: BooleanValue): Value =
    BooleanValue(lhs.value == rhs.value)

private fun compareEqualNumeric(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value == rhs.value)

private fun compareEqualString(lhs: StringValue, rhs: StringValue): Value =
    BooleanValue(lhs.value == rhs.value)

private fun compareEqualObject(lhs: ObjectValue, rhs: ObjectValue): Value =
    BooleanValue(lhs == rhs)

private fun compareNotEqualBoolean(lhs: BooleanValue, rhs: BooleanValue): Value =
    BooleanValue(lhs.value != rhs.value)

private fun compareNotEqualNumeric(lhs: NumericValue, rhs: NumericValue): Value =
    BooleanValue(lhs.value != rhs.value)

private fun compareNotEqualString(lhs: StringValue, rhs: StringValue): Value =
    BooleanValue(lhs.value != rhs.value)

private fun compareNotEqualObject(lhs: ObjectValue, rhs: ObjectValue): Value =
    BooleanValue(lhs != rhs)
