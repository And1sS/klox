package interpreter

import lexer.tokenize
import parser.Matched
import parser.ProgramToken
import parser.Unmatched
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
