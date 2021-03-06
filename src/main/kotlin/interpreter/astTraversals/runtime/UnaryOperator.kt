package interpreter.astTraversals.runtime

import ast.OperatorType
import ast.UnaryOperatorExpression
import exception.EvaluationException
import interpreter.BooleanValue
import interpreter.Environment
import interpreter.NumericValue
import interpreter.Value
import parser.validateGrammar
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
    validateGrammar(value is T)

    evaluator(value)
}
