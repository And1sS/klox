package interpreter

import exception.EvaluationException
import parser.ast.BooleanValue
import parser.ast.NumericValue
import parser.ast.OperatorType
import parser.ast.UnaryOperatorExpression
import parser.ast.Value
import kotlin.reflect.KClass

fun evaluateUnaryOperatorExpression(
    expression: UnaryOperatorExpression,
    evaluationEnvironment: Environment
): Value {
    val operatorType = expression.operatorType
    val exprValue = evaluateExpression(expression.expr, evaluationEnvironment)

    val signature = UnaryOperatorSignature(operatorType, exprValue::class)
    return unaryOperatorEvaluators[signature]?.invoke(exprValue)
        ?: throw EvaluationException(
            "Invalid evaluation: could not find unary operator "
                    + "$operatorType for ${exprValue::class}"
        )
}

private typealias UnaryOperatorEvaluator = (Value) -> Value
private typealias UnaryOperatorSignature = Pair<OperatorType, KClass<out Value>>

private val unaryOperatorEvaluators: Map<UnaryOperatorSignature, UnaryOperatorEvaluator> =
    mapOf(
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
        "This branch shouldn't have been reached"
    }
    evaluator(value)
}
