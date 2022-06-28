package interpreter.astTraversals.semantic

import ast.AssignmentExpression
import ast.BinaryOperatorExpression
import ast.CallExpression
import ast.Expression
import ast.FieldAccessExpression
import ast.IdentifierExpression
import ast.LabelExpression
import ast.Literal
import ast.ResolvedIdentifierExpression
import ast.UnaryOperatorExpression
import ast.UnresolvedIdentifierExpression
import exception.EvaluationException
import interpreter.Environment
import parser.validateRuntime

fun resolveExpression(expr: Expression, evaluationEnvironment: Environment): Expression =
    when (expr) {
        is Literal -> expr
        is LabelExpression -> resolveLabelExpression(expr, evaluationEnvironment)
        is UnaryOperatorExpression -> resolveUnaryOperatorExpression(expr, evaluationEnvironment)
        is BinaryOperatorExpression -> resolveBinaryOperatorExpression(expr, evaluationEnvironment)
        is AssignmentExpression -> resolveAssignmentExpression(expr, evaluationEnvironment)
        is CallExpression -> resolveFunctionCallExpression(expr, evaluationEnvironment)
    }

private fun resolveFunctionCallExpression(
    expr: CallExpression,
    evaluationEnvironment: Environment
): CallExpression {
    val functionExpression = resolveExpression(expr.function, evaluationEnvironment)
    val arguments: List<Expression> = expr.arguments
        .map { arg -> resolveExpression(arg, evaluationEnvironment) }

    return CallExpression(functionExpression, arguments)
}

private fun resolveAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): AssignmentExpression {
    validateRuntime(expr.label !is IdentifierExpression || expr.label.name != "this") {
        "Cannot assign to 'this'"
    }
    return AssignmentExpression(
        label = resolveLabelExpression(expr.label, evaluationEnvironment),
        expr = resolveExpression(expr.expr, evaluationEnvironment)
    )
}

private fun resolveLabelExpression(
    expr: LabelExpression,
    evaluationEnvironment: Environment
): LabelExpression = when (expr) {
    is FieldAccessExpression -> FieldAccessExpression(
        lhs = resolveExpression(expr.lhs, evaluationEnvironment),
        memberName = expr.memberName
    )
    is UnresolvedIdentifierExpression -> evaluationEnvironment.resolveVariable(expr)
    // this branch shouldn't be reached
    is ResolvedIdentifierExpression ->
        throw EvaluationException("Trying to re-resolve variable")
}

private fun resolveBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): BinaryOperatorExpression = BinaryOperatorExpression(
    operatorType = expression.operatorType,
    lhs = resolveExpression(expression.lhs, evaluationEnvironment),
    rhs = resolveExpression(expression.rhs, evaluationEnvironment)
)

private fun resolveUnaryOperatorExpression(
    expression: UnaryOperatorExpression,
    evaluationEnvironment: Environment
): UnaryOperatorExpression = UnaryOperatorExpression(
    expression.operatorType,
    resolveExpression(expression.expr, evaluationEnvironment)
)

