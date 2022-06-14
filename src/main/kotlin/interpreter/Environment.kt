package interpreter

import ast.ResolvedIdentifierExpression
import ast.UnresolvedIdentifierExpression
import exception.EvaluationException
import exception.SemanticError

class Environment(private val parentEnvironment: Environment? = null) {
    private val variables: MutableMap<String, Value> = mutableMapOf()

    fun createVariable(name: String, value: Value) {
        if (name in variables)
            throw SemanticError("Redeclaration of variable: $name")
        variables[name] = value
    }

    fun assignVariable(identifier: ResolvedIdentifierExpression, value: Value): Value =
        lookupEnvironment(identifier.depth)
            ?.variables
            ?.computeIfPresent(identifier.name) { _, _ -> value }
            ?: throw EvaluationException("Undefined variable: ${identifier.name}")

    fun getVariableValue(identifier: ResolvedIdentifierExpression): Value =
        lookupEnvironment(identifier.depth)
            ?.variables
            ?.get(identifier.name)
            ?: throw EvaluationException("Undefined variable: ${identifier.name}")

    private fun lookupEnvironment(depth: Int): Environment? {
        var currentEnvironment: Environment? = this
        for (i in 1..depth) {
            currentEnvironment = currentEnvironment?.parentEnvironment
        }

        return currentEnvironment
    }

    fun resolveVariable(identifier: UnresolvedIdentifierExpression): ResolvedIdentifierExpression {
        var i = 0
        var currentEnvironment: Environment? = this
        while (currentEnvironment != null) {
            if (identifier.name in currentEnvironment.variables)
                return ResolvedIdentifierExpression(identifier.name, i)

            currentEnvironment = currentEnvironment.parentEnvironment
            i++
        }

        throw SemanticError("Unresolved variable ${identifier.name}")
    }
}
