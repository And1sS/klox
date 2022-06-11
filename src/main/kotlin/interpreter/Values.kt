package interpreter

import ast.BlockStatement

sealed class Value

object NilValue : Value()

data class BooleanValue(val value: Boolean) : Value()

data class NumericValue(val value: Double) : Value()

data class StringValue(val value: String) : Value()

// TODO: implement objects properly
class ObjectValue : Value()

// TODO: add lambda functions
sealed class FunctionValue(val argNumber: Int) : Value()

data class LoxFunctionValue(
    val argNames: List<String>,
    val body: BlockStatement,
    val capturingEnvironment: Environment
) : FunctionValue(argNames.size) {
    override fun toString(): String = "LoxFunctionValue(args = $argNames, body = $body)"
}

class NativeFunctionValue(
    val name: String,
    argNumber: Int,
    val call: (List<Value>) -> Value
) : FunctionValue(argNumber) {
    override fun toString(): String = "NativeFunctionValue(name = $name, argNumber = $argNumber)"
}