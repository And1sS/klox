package interpreter

import lexer.tokenize
import parser.Matched
import parser.NodeToken
import parser.ProgramToken
import parser.Rule
import parser.Unmatched
import parser.andRule
import parser.ast.Expression
import parser.ast.PrintStatement
import parser.orRule
import parser.rules.eofRule
import parser.rules.expressionRule
import parser.rules.programRule
import parser.toParsingContext

fun interpret(program: String) {
    val initialContext = program.tokenize().toParsingContext()

    val matchResult = programRule.match(initialContext)
    if (matchResult is Unmatched) {
        throw RuntimeException("Unable to parse program")
    }
    require(matchResult is Matched) {
        "This branch shouldn't have been reached"
    }

    val programToken = matchResult.token
    require(programToken is ProgramToken) {
        "Invalid grammar: top level token is not a program token"
    }

    val globalEnvironment = Environment()
    for (declaration in programToken.declarations) {
        evaluateDeclaration(declaration, globalEnvironment)
    }

    val finalContext = matchResult.newCtx
    if (finalContext.currentIndex != initialContext.tokens.lastIndex) {
        throw RuntimeException("Could not interpret program on: ${finalContext.currentToken().position}")
    }
}

fun liveInterpret() {
    val liveInterpretRule: Rule = orRule(andRule(expressionRule, eofRule) { it[0] }, programRule)

    println("Line interpreting session started")
    val globalEnvironment = Environment()

    while (true) {
        val line = readln()

        if (line.trim().isBlank())
            continue

        val currentContext = line.tokenize().toParsingContext()
        val matchResult = liveInterpretRule.match(currentContext)
        if (matchResult is Unmatched) {
            println("Parsing error")
            continue
        }
        require(matchResult is Matched) {
            "This branch shouldn't have been reached"
        }

        var lineToken = matchResult.token
        if (lineToken is NodeToken) {
            if (lineToken.node !is Expression) {
                println("Parsing error")
                continue
            }
            evaluateDeclaration(PrintStatement(lineToken.node as Expression), globalEnvironment)
            continue
        }

        require(lineToken is ProgramToken) {
            "Invalid grammar: top level token is not a program token"
        }

        for (declaration in lineToken.declarations) {
            evaluateDeclaration(declaration, globalEnvironment)
        }

        val finalContext = matchResult.newCtx
        if (finalContext.currentIndex != currentContext.tokens.lastIndex) {
            println("Could not interpret line on position: ${finalContext.currentToken().position.position}")
        }
    }
}
