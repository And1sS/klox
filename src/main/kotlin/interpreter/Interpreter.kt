package interpreter

import lexer.tokenize
import parser.Matched
import parser.NodeToken
import parser.ProgramToken
import parser.Rule
import parser.Unmatched
import parser.andRule
import ast.Expression
import ast.PrintStatement
import parser.orRule
import parser.rules.eofRule
import parser.rules.expressionRule
import parser.rules.programRule
import parser.toParsingContext
import parser.validateGrammar

fun interpret(program: String) {
    val initialContext = program.tokenize().toParsingContext()

    val matchResult = programRule.match(initialContext)
    if (matchResult is Unmatched) {
        throw RuntimeException("Unable to parse program")
    }
    validateGrammar(matchResult is Matched)

    val programToken = matchResult.token
    validateGrammar(programToken is ProgramToken)

    val globalEnvironment = Environment()
    for (declaration in programToken.declarations) {
        executeDeclaration(declaration, globalEnvironment)
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
        validateGrammar(matchResult is Matched)

        val lineToken = matchResult.token
        if (lineToken is NodeToken) {
            if (lineToken.node !is Expression) {
                println("Parsing error")
                continue
            }
            executeDeclaration(PrintStatement(lineToken.node), globalEnvironment)
            continue
        }
        validateGrammar(lineToken is ProgramToken)

        for (declaration in lineToken.declarations) {
            executeDeclaration(declaration, globalEnvironment)
        }

        val finalContext = matchResult.newCtx
        if (finalContext.currentIndex != currentContext.tokens.lastIndex) {
            println("Could not interpret line on position: ${finalContext.currentToken().position.position}")
        }
    }
}
