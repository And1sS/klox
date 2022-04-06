package parser

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