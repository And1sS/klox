package interpreter

import ast.IdentifierExpression
import ast.ResolvedIdentifierExpression
import ast.UnresolvedIdentifierExpression
import ast.Value
import exception.EvaluationException
import exception.SemanticError

class Environment(private val parentEnvironment: Environment?) {
    private val variables: MutableMap<String, Value> = mutableMapOf()

    constructor() : this(null)

    fun createVariable(name: String, value: Value) {
        variables[name] = value
    }

    fun assignVariable(identifier: ResolvedIdentifierExpression, value: Value) {
        var currentEnvironment = this
        for (i in 1..identifier.depth) {
            currentEnvironment = currentEnvironment.parentEnvironment
                ?: throw EvaluationException("Undefined variable: ${identifier.name}")
        }

        if (identifier.name in currentEnvironment.variables)
            throw EvaluationException("Undefined variable: ${identifier.name}")
        currentEnvironment.variables[identifier.name] = value
    }

    fun getVariableValue(identifier: ResolvedIdentifierExpression): Value {
        var currentEnvironment = this
        for (i in 1..identifier.depth) {
            currentEnvironment = currentEnvironment.parentEnvironment
                ?: throw EvaluationException("Undefined variable: ${identifier.name}")
        }

        return currentEnvironment.variables[identifier.name]
            ?: throw EvaluationException("Undefined variable: ${identifier.name}")
    }

    fun resolveVariable(identifier: UnresolvedIdentifierExpression): ResolvedIdentifierExpression {
        var i = 0
        var currentEnvironment: Environment? = this
        while (currentEnvironment != null) {

            if (identifier.name in currentEnvironment.variables) {
                return ResolvedIdentifierExpression(identifier.name, i)
            }

            currentEnvironment = currentEnvironment.parentEnvironment
            i++
        }

        throw SemanticError("Unresolved variable ${identifier.name}")
    }
}
