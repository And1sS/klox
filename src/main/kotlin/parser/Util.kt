package parser

import exception.EvaluationException
import parser.ast.BooleanValue
import parser.ast.Expression
import parser.ast.NilValue
import parser.ast.NumericValue
import parser.ast.ObjectValue
import parser.ast.OperatorType
import parser.ast.StringValue
import parser.ast.Value
import parser.ast.operatorMapping
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
    is BooleanValue -> this.value.toString()
    is NumericValue -> this.value.toString()
    is StringValue -> this.value
    is ObjectValue -> this.toString()
}

// To add destructuring capabilities for lists with 6 elements
operator fun <T> List<T>.component6(): T {
    return get(5)
}

fun validateGrammar(value: Boolean) {
    contract {
        returns() implies (value)
    }
    require(value) { "Invalid grammar" }
}

fun validateBoolean(value: Value) {
    contract {
        returns() implies (value is BooleanValue)
    }
    if (value !is BooleanValue) {
        throw EvaluationException("Could not interpret value of type ${value::class} as Boolean")
    }
}
