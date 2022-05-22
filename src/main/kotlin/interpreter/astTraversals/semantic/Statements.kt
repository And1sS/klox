package interpreter.astTraversals.semantic

import ast.BlockStatement
import ast.Declaration
import ast.ExpressionStatement
import ast.ForStatement
import ast.FunctionDeclaration
import ast.IfStatement
import ast.NilValue
import ast.PrintStatement
import ast.ReturnStatement
import ast.Statement
import ast.VarDeclaration
import ast.WhileStatement
import exception.SemanticError
import interpreter.Environment

fun resolveDeclaration(
    declaration: Declaration,
    evaluationEnvironment: Environment
): Declaration = when (declaration) {
    is VarDeclaration -> resolveVarDeclaration(declaration, evaluationEnvironment)
    is FunctionDeclaration -> resolveFunctionDeclaration(declaration, evaluationEnvironment)
    is Statement -> resolveStatement(declaration, evaluationEnvironment)
}

private fun resolveVarDeclaration(
    declaration: VarDeclaration,
    evaluationEnvironment: Environment
): VarDeclaration {
    val initializationExpression = declaration.initializationExpression?.let {
        resolveExpression(it, evaluationEnvironment)
    }

    evaluationEnvironment.createVariable(declaration.identifier, NilValue)

    return VarDeclaration(declaration.identifier, initializationExpression)
}

private fun resolveFunctionDeclaration(
    declaration: FunctionDeclaration,
    evaluationEnvironment: Environment
): FunctionDeclaration {
    val functionEnvironment = Environment(evaluationEnvironment)

    for (arg in declaration.argNames) {
        functionEnvironment.createVariable(arg, NilValue)
    }

    val body = resolveBlockStatement(declaration.body, functionEnvironment)
    evaluationEnvironment.createVariable(declaration.identifier, NilValue)

    return FunctionDeclaration(declaration.identifier, declaration.argNames, body)
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
    return when (statement.initializer) {
        is VarDeclaration -> ForStatement(
            initializer = resolveVarDeclaration(statement.initializer, evaluationEnvironment),
            condition = statement.condition?.let { resolveExpression(it, evaluationEnvironment) },
            increment = statement.increment?.let { resolveExpression(it, evaluationEnvironment) },
            body = resolveStatement(statement.body, evaluationEnvironment)
        )
        is ExpressionStatement -> ForStatement(
            initializer = resolveExpressionStatement(statement.initializer, evaluationEnvironment),
            condition = statement.condition?.let { resolveExpression(it, evaluationEnvironment) },
            increment = statement.increment?.let { resolveExpression(it, evaluationEnvironment) },
            body = resolveStatement(statement.body, evaluationEnvironment)
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
