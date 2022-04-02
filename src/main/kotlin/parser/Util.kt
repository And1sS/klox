package parser

fun List<ParserToken>.toOperatorTypeAndOperand(): Pair<OperatorType, Expression> {
    val (operator, operand) = this
    require(operand is Expression) {
        "Invalid grammar: operator argument is not an expression"
    }
    require(operator is SingleParserToken) {
        "Invalid grammar: operator token is not a single token"
    }
    val operatorType = operatorMapping[operator.lexerToken::class]
        ?: throw RuntimeException("Invalid grammar: unknown operator")
    return Pair(operatorType, operand)
}