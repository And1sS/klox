package interpreter

import exception.EvaluationException
import parser.ast.IdentifierExpression
import parser.ast.Value


class Environment(private val parentEnvironment: Environment?) {
    private val variables: MutableMap<IdentifierExpression, Value> = mutableMapOf()

    constructor() : this(null)

    fun createVariable(name: IdentifierExpression, value: Value) {
        variables[name] = value
    }

    fun assignVariable(name: IdentifierExpression, value: Value) {
        if (variables.containsKey(name)) {
            variables[name] = value
        } else if (parentEnvironment != null) {
            parentEnvironment.assignVariable(name, value)
        } else {
            throw EvaluationException("Undefined variable: ${name.name}")
        }
    }

    fun getVariableValue(name: IdentifierExpression): Value =
        variables.getOrElse(name) {
            parentEnvironment?.getVariableValue(name)
                ?: throw EvaluationException("Undefined variable: ${name.name}")
        }
}
