package interpreter

import ast.BlockStatement
import ast.FunctionDeclaration
import ast.VarDeclaration

sealed class Value

object NilValue : Value()

data class BooleanValue(val value: Boolean) : Value()

data class NumericValue(val value: Double) : Value()

data class StringValue(val value: String) : Value()

// TODO: add default constructor if no constructor is present
data class ObjectValue(
    val loxClass: ClassValue,
    val enclosedEnvironment: Environment
) : Value()

data class ClassValue(
    val name: String,
    val constructor: FunctionDeclaration,
    val fields: List<VarDeclaration>,
    val methods: List<FunctionDeclaration>,
    val capturedEnvironment: Environment
) : Value()

// TODO: add lambda functions
sealed class FunctionValue(val argNumber: Int) : Value()

data class LoxFunctionValue(
    val argNames: List<String>,
    val body: BlockStatement,
    val capturedEnvironment: Environment
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