package interpreter

import interpreter.astTraversals.runtime.executeDeclaration
import interpreter.astTraversals.semantic.resolveDeclaration
import lexer.tokenize
import parser.Matched
import parser.ProgramToken
import parser.Unmatched
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

    val finalContext = matchResult.newCtx
    if (finalContext.currentIndex != initialContext.tokens.lastIndex) {
        throw RuntimeException("Syntax error at: ${finalContext.currentToken().position}")
    }

    val programToken = matchResult.token
    validateGrammar(programToken is ProgramToken)

    val resolutionGlobalEnvironment = Environment().also(::importNativeFunctions)
    val resolvedDeclarations = programToken.declarations.map { declaration ->
        resolveDeclaration(declaration, resolutionGlobalEnvironment)
    }

    val globalEnvironment = Environment().also(::importNativeFunctions)
    for (declaration in resolvedDeclarations) {
        executeDeclaration(declaration, globalEnvironment)
    }
}
