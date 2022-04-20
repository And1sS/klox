package parser.ast

sealed class Declaration : AbstractSyntaxNode()

data class VarDeclaration(
    val identifier: IdentifierExpression,
    val value: Expression?
) : Declaration()

sealed class Statement : Declaration()

data class ReturnStatement(val expr: Expression) : Statement()

data class IfStatement(
    val condition: Expression,
    val body: Statement,
    val elseBody: Statement?
) : Statement()

data class WhileStatement(
    val condition: Expression,
    val body: Statement
) : Statement()

data class ForStatement private constructor(
    val initializer: Declaration?,
    val condition: Expression?,
    val increment: Expression?,
    val body: Statement
) : Statement() {
    constructor(
        initializer: VarDeclaration?,
        condition: Expression?,
        increment: Expression?,
        body: Statement
    ) : this(initializer as? Declaration, condition, increment, body)

    constructor(
        initializer: ExpressionStatement?,
        condition: Expression?,
        increment: Expression?,
        body: Statement
    ) : this(initializer as? Declaration, condition, increment, body)
}

data class PrintStatement(val expr: Expression) : Statement()

data class ExpressionStatement(val expr: Expression) : Statement()

data class BlockStatement(val declarations: List<Declaration>) : Statement()
