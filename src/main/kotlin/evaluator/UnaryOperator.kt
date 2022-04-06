package evaluator

import parser.BooleanValue
import parser.NumericValue
import parser.OperatorType
import parser.UnaryOperatorExpression
import parser.Value
import kotlin.reflect.KClass

fun evaluateUnaryOperatorExpression(expression: UnaryOperatorExpression): Value {
    val operatorType = expression.operatorType
    val exprValue = evaluate(expression.expr)

    val signature = UnaryOperatorSignature(operatorType, exprValue::class)
    return unaryOperatorEvaluators[signature]?.invoke(exprValue)
        ?: throw RuntimeException(
            "Invalid evaluation: could not find unary operator "
                    + "$operatorType for ${exprValue::class}"
        )
}

private typealias UnaryOperatorEvaluator = (Value) -> Value
private typealias UnaryOperatorSignature = Pair<OperatorType, KClass<out Value>>

private val unaryOperatorEvaluators =
    mapOf<UnaryOperatorSignature, UnaryOperatorEvaluator>(
        UnaryOperatorSignature(OperatorType.Minus, NumericValue::class)
                to unaryOperatorEvaluator(::negateNumerical),
        UnaryOperatorSignature(OperatorType.Bang, BooleanValue::class)
                to unaryOperatorEvaluator(::negateBoolean)
    )

private fun negateNumerical(value: NumericValue): Value = NumericValue(-value.value)
private fun negateBoolean(value: BooleanValue): Value = BooleanValue(!value.value)

private inline fun <reified T : Value> unaryOperatorEvaluator(
    crossinline evaluator: (T) -> Value
): UnaryOperatorEvaluator = { value ->
    require(value is T) {
        "Critical error: this branch shouldn't have been reached"
    }
    evaluator(value)
}

