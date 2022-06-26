package interpreter.astTraversals.semantic

import ast.BlockStatement
import ast.ClassDeclaration
import ast.FunctionDeclaration
import ast.VarDeclaration
import exception.SemanticError
import interpreter.Environment
import interpreter.NilValue

fun resolveClassDeclaration(
    klass: ClassDeclaration,
    evaluationEnvironment: Environment
): ClassDeclaration {
    val objectEnvironment = Environment(evaluationEnvironment)

    evaluationEnvironment.createVariable(klass.name, NilValue)

    if (klass.constructors.size > 1)
        throw SemanticError("More than one constructor declared in class ${klass.name}")
    val constructor: FunctionDeclaration = klass.constructors.firstOrNull()
        ?: FunctionDeclaration(klass.name, emptyList(), BlockStatement(emptyList()))

    for (member in klass.fields + klass.methods) {
        objectEnvironment.createVariable(member.name, NilValue) // TODO: add Environment.declareVariable()
    }

    return ClassDeclaration(
        name = klass.name,
        fields = klass.fields.map { field -> resolveField(field, objectEnvironment) },
        methods = klass.methods.map { method -> resolveMethod(method, objectEnvironment) },
        constructors = resolveMethod(constructor, objectEnvironment).let(::listOf)
    )
}

private fun resolveField(
    field: VarDeclaration,
    evaluationEnvironment: Environment
): VarDeclaration = VarDeclaration(
    field.name,
    field.initializationExpression?.let { resolveExpression(it, evaluationEnvironment) }
)

private fun resolveMethod(
    method: FunctionDeclaration,
    objectEnvironment: Environment
): FunctionDeclaration {
    val functionEnvironment = Environment(objectEnvironment)

    for (arg in method.argNames) {
        functionEnvironment.createVariable(arg, NilValue)
    }

    return FunctionDeclaration(
        name = method.name,
        argNames = method.argNames,
        body = resolveBlockStatement(method.body, functionEnvironment)
    )
}