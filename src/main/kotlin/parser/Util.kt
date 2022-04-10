package parser

import parser.ast.BooleanValue
import parser.ast.Expression
import parser.ast.NilValue
import parser.ast.NumericValue
import parser.ast.ObjectValue
import parser.ast.OperatorType
import parser.ast.StringValue
import parser.ast.Value
import parser.ast.operatorMapping

fun List<ParserToken>.toOperatorTypeAndOperand(): Pair<OperatorType, Expression> {
    val (operator, operand) = this
    require(operator is SymbolicToken) {
        "Invalid grammar: operator token is not a single token"
    }
    require(operand is NodeToken && operand.node is Expression) {
        "Invalid grammar: operator argument is not an expression"
    }
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