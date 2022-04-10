package parser.ast

sealed class Declaration : AbstractSyntaxNode()

sealed class Statement : Declaration()

data class VarDeclaration(
    val identifier: IdentifierExpression,
    val value: Expression?
) : Declaration()

data class PrintStatement(val expr: Expression) : Statement()

data class ExpressionStatement(val expr: Expression) : Statement()

data class BlockStatement(val declarations: List<Declaration>) : Statement()