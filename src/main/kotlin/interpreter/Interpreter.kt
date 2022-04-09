package interpreter

import lexer.tokenize
import parser.Matched
import parser.ProgramToken
import parser.Unmatched
import parser.programRule
import parser.toParsingContext

fun interpret(program: String) {
    val parsingContext = program.tokenize().toParsingContext()

    val matchResult = programRule.match(parsingContext)
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
}
