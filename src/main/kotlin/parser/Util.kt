package parser

import ast.Expression
import ast.OperatorType
import ast.operatorMapping
import exception.EvaluationException
import exception.GrammarException
import interpreter.BooleanValue
import interpreter.FunctionValue
import interpreter.NilValue
import interpreter.NumericValue
import interpreter.ObjectValue
import interpreter.StringValue
import interpreter.Value
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
    is FunctionValue -> toString()
    is ObjectValue -> toString()
    is interpreter.ClassValue -> toString() // TODO: fix
}

// To enhance list destructuring capabilities
operator fun <T> List<T>.component6(): T = get(5)
operator fun <T> List<T>.component7(): T = get(6)
operator fun <T> List<T>.component8(): T = get(7)

fun validateGrammar(value: Boolean) {
    contract {
        returns() implies (value)
    }
    if (!value) {
        throwInvalidGrammar()
    }
}

fun throwInvalidGrammar(): Nothing = throw GrammarException("Invalid grammar")

fun validateRuntime(value: Boolean, errorMessage: () -> String = { "Runtime exception" }) {
    contract {
        returns() implies (value)
    }
    if (!value) {
        throw EvaluationException(errorMessage())
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
