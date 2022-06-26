package interpreter.astTraversals.semantic

import ast.BlockStatement
import ast.ClassDeclaration
import ast.Declaration
import ast.ExpressionStatement
import ast.ForStatement
import ast.FunctionDeclaration
import ast.IfStatement
import ast.PrintStatement
import ast.ReturnStatement
import ast.Statement
import ast.VarDeclaration
import ast.WhileStatement
import exception.SemanticError
import interpreter.Environment
import interpreter.NilValue

fun resolveDeclaration(
    declaration: Declaration,
    evaluationEnvironment: Environment
): Declaration = when (declaration) {
    is VarDeclaration -> resolveVarDeclaration(declaration, evaluationEnvironment)
    is FunctionDeclaration -> resolveFunctionDeclaration(declaration, evaluationEnvironment)
    is ClassDeclaration -> resolveClassDeclaration(declaration, evaluationEnvironment)
    is Statement -> resolveStatement(declaration, evaluationEnvironment)
}

private fun resolveVarDeclaration(
    declaration: VarDeclaration,
    evaluationEnvironment: Environment
): VarDeclaration {
    val initializationExpression = declaration.initializationExpression?.let {
        resolveExpression(it, evaluationEnvironment)
    }

    evaluationEnvironment.createVariable(declaration.name, NilValue)

    return VarDeclaration(declaration.name, initializationExpression)
}

private fun resolveFunctionDeclaration(
    function: FunctionDeclaration,
    evaluationEnvironment: Environment
): FunctionDeclaration {
    val functionEnvironment = Environment(evaluationEnvironment)

    for (arg in function.argNames) {
        functionEnvironment.createVariable(arg, NilValue)
    }

    evaluationEnvironment.createVariable(function.name, NilValue)
    val body = resolveBlockStatement(function.body, functionEnvironment)

    return FunctionDeclaration(function.name, function.argNames, body)
}

private fun resolveStatement(
    statement: Statement,
    evaluationEnvironment: Environment
): Statement = when (statement) {
    is ExpressionStatement -> resolveExpressionStatement(statement, evaluationEnvironment)
    is PrintStatement -> resolvePrintStatement(statement, evaluationEnvironment)
    is BlockStatement -> resolveBlockStatement(statement, evaluationEnvironment)
    is IfStatement -> resolveIfStatement(statement, evaluationEnvironment)
    is WhileStatement -> resolveWhileStatement(statement, evaluationEnvironment)
    is ForStatement -> resolveForStatement(statement, evaluationEnvironment)
    is ReturnStatement -> resolveReturnStatement(statement, evaluationEnvironment)
}

private fun resolveExpressionStatement(
    statement: ExpressionStatement,
    evaluationEnvironment: Environment
): ExpressionStatement =
    ExpressionStatement(resolveExpression(statement.expr, evaluationEnvironment))

private fun resolvePrintStatement(
    statement: PrintStatement,
    evaluationEnvironment: Environment
): PrintStatement =
    PrintStatement(resolveExpression(statement.expr, evaluationEnvironment))

fun resolveBlockStatement(
    statement: BlockStatement,
    evaluationEnvironment: Environment
): BlockStatement {
    val blockEnvironment = Environment(evaluationEnvironment)

    val declarations = statement.declarations.map { declaration ->
        resolveDeclaration(declaration, blockEnvironment)
    }

    return BlockStatement(declarations)
}

private fun resolveIfStatement(
    statement: IfStatement,
    evaluationEnvironment: Environment
): IfStatement = IfStatement(
    condition = resolveExpression(statement.condition, evaluationEnvironment),
    body = resolveStatement(statement.body, evaluationEnvironment),
    elseBody = statement.elseBody?.let { resolveStatement(it, evaluationEnvironment) }
)

private fun resolveWhileStatement(
    statement: WhileStatement,
    evaluationEnvironment: Environment
): WhileStatement = WhileStatement(
    condition = resolveExpression(statement.condition, evaluationEnvironment),
    body = resolveStatement(statement.body, evaluationEnvironment)
)

private fun resolveForStatement(
    statement: ForStatement,
    evaluationEnvironment: Environment
): ForStatement {
    val condition = statement.condition?.let { resolveExpression(it, evaluationEnvironment) }
    val increment = statement.increment?.let { resolveExpression(it, evaluationEnvironment) }
    val body = resolveStatement(statement.body, evaluationEnvironment)

    return when (statement.initializer) {
        is VarDeclaration -> ForStatement(
            initializer = resolveVarDeclaration(statement.initializer, evaluationEnvironment),
            condition, increment, body
        )
        is ExpressionStatement -> ForStatement(
            initializer = resolveExpressionStatement(statement.initializer, evaluationEnvironment),
            condition, increment, body
        )
        // TODO: add unreachable exception
        else -> throw SemanticError("This branch shouldn't have been reached")
    }
}

private fun resolveReturnStatement(
    statement: ReturnStatement,
    evaluationEnvironment: Environment
): ReturnStatement =
    ReturnStatement(resolveExpression(statement.expr, evaluationEnvironment))
