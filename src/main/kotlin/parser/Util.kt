package parser

import exception.EvaluationException
import ast.BooleanValue
import ast.Expression
import ast.FunctionValue
import ast.NilValue
import ast.NumericValue
import ast.ObjectValue
import ast.OperatorType
import ast.StringValue
import ast.Value
import ast.operatorMapping
import kotlin.contracts.contract

fun List<ParserToken>.toOperatorTypeAndOperand(): Pair<OperatorType, Expression> {
    val (operator, operand) = this
    validateGrammar(operator is SymbolicToken)
    validateGrammar(operand is NodeToken && operand.node is Expression)

    val operatorType = operatorMapping[operator.lexerToken::class]
        ?: throw RuntimeException("Invalid grammar: unknown operator")
    return Pair(operatorType, operand.node)
}

fun Value.asString(): String = when (this) {
    is NilValue -> "nil"
    is BooleanValue -> value.toString()
    is NumericValue -> value.toString()
    is StringValue -> value
    is ObjectValue -> toString()
    is FunctionValue -> toString()
}

// To enhance list destructuring capabilities
operator fun <T> List<T>.component6(): T = get(5)
operator fun <T> List<T>.component7(): T = get(6)
operator fun <T> List<T>.component8(): T = get(7)

fun validateGrammar(value: Boolean) {
    contract {
        returns() implies (value)
    }
    require(value) { "Invalid grammar" }
}

fun throwInvalidGrammar(): Unit = validateGrammar(false)

fun validateRuntime(value: Boolean, errorMessage: () -> String = { "Runtime exception" }) {
    contract {
        returns() implies (value)
    }
    if (!value) {
        throw EvaluationException(errorMessage.let { it() })
    }
}

fun validateRuntimeBoolean(value: Value) {
    contract {
        returns() implies (value is BooleanValue)
    }
    if (value !is BooleanValue) {
        throw EvaluationException("Could not interpret value of type ${value::class} as Boolean")
    }
}
