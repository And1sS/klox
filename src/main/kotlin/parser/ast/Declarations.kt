package parser.ast

sealed class Declaration : AbstractSyntaxNode()

data class VarDeclaration(
    val identifier: IdentifierExpression,
    val value: Expression?
) : Declaration()

sealed class Statement : Declaration()

data class IfStatement(
    val condition: Expression,
    val body: Statement,
    val elseBody: Statement?
) : Statement()

data class PrintStatement(val expr: Expression) : Statement()

data class ExpressionStatement(val expr: Expression) : Statement()

data class BlockStatement(val declarations: List<Declaration>) : Statement()
