package lexer

fun tokenize(source: String): List<Token> =
    source.split(Regex("\\r?\\n"))
        .flatMapIndexed(::tokenizeLine)

private data class ParsingContext(
    val remainingString: String,
    val position: Position
) {
    fun skipChars(amount: Int): ParsingContext =
        ParsingContext(
            remainingString.drop(amount),
            position.skipChars(amount)
        )
}

private fun tokenizeLine(lineNumber: Int, line: String): List<Token> {
    var currentContext = ParsingContext(line, Position(lineNumber, 0))
    val result = mutableListOf<Token>()

    while (true) {
        val (currentToken, nextContext) = nextToken(currentContext)
        if (currentToken is EndOfLine) {
            return result
        } else if (currentToken is Unmatched) {
            throw RuntimeException("Unmatched token on position: ${currentToken.position}")
        }

        result.add(currentToken)
        currentContext = nextContext
    }
}

private fun nextToken(parsingContext: ParsingContext): Pair<Token, ParsingContext> {
    val withoutWhitespacesAndComments = skipWhitespacesAndComments(parsingContext)
    val (source, position) = withoutWhitespacesAndComments

    val token: Token = when {
        source.isEmpty() -> EndOfLine(position)
        source.startsWith("(") -> LeftParen(position)
        source.startsWith(")") -> RightParen(position)
        source.startsWith("{") -> LeftBrace(position)
        source.startsWith("}") -> RightBrace(position)
        source.startsWith(",") -> Comma(position)
        source.startsWith(".") -> Dot(position)
        source.startsWith("-") -> Minus(position)
        source.startsWith("+") -> Plus(position)
        source.startsWith(";") -> Semicolon(position)
        source.startsWith("/") -> Slash(position)
        source.startsWith("*") -> Star(position)

        source.startsWith("!=") -> BangEqual(position)
        source.startsWith("==") -> EqualEqual(position)
        source.startsWith(">=") -> GreaterEqual(position)
        source.startsWith("<=") -> LessEqual(position)

        source.startsWith("!") -> Bang(position)
        source.startsWith("=") -> Equal(position)
        source.startsWith(">") -> Greater(position)
        source.startsWith("<") -> Less(position)

        source.startsWith("and") -> And(position)
        source.startsWith("class") -> Class(position)
        source.startsWith("else") -> Else(position)
        source.startsWith("false") -> False(position)
        source.startsWith("fun") -> Fun(position)
        source.startsWith("for") -> For(position)
        source.startsWith("if") -> If(position)
        source.startsWith("nil") -> Nil(position)
        source.startsWith("or") -> Or(position)
        source.startsWith("print") -> Print(position)
        source.startsWith("return") -> Return(position)
        source.startsWith("super") -> Super(position)
        source.startsWith("this") -> This(position)
        source.startsWith("true") -> True(position)
        source.startsWith("var") -> Var(position)
        source.startsWith("while") -> While(position)

        else -> parseLiteral(withoutWhitespacesAndComments)
    }

    return Pair(token, withoutWhitespacesAndComments.skipChars(token.length))
}

private val whitespacesCommentsRegex = Regex("([\\s\\t]+(?://.*)?)")
private fun skipWhitespacesAndComments(parsingContext: ParsingContext): ParsingContext {
    val matchEndIndex = firstGroup(parsingContext.remainingString, whitespacesCommentsRegex)?.value?.length
        ?: return parsingContext

    return parsingContext.skipChars(matchEndIndex)
}

private fun parseLiteral(parsingContext: ParsingContext): Token {
    val asNumberLiteral = tryParseNumberLiteral(parsingContext)
    if (asNumberLiteral is NumberLiteral) return asNumberLiteral

    val asStringLiteral = tryParseStringLiteral(parsingContext)
    if (asStringLiteral is StringLiteral) return asStringLiteral

    return tryParseIdentifier(parsingContext)
}

val numberLiteralRegex = Regex("(\\d+\\.?\\d+|\\d)")
private fun tryParseNumberLiteral(parsingContext: ParsingContext): Token {
    val numberLiteral: String = firstGroup(parsingContext.remainingString, numberLiteralRegex)?.value
        ?: return Unmatched(parsingContext.position)

    return NumberLiteral(numberLiteral.toDouble(), numberLiteral.length, parsingContext.position)
}

val stringLiteralRegex = Regex("(\"[^\"]*\")")
private fun tryParseStringLiteral(parsingContext: ParsingContext): Token {
    val stringLiteral: String = firstGroup(parsingContext.remainingString, stringLiteralRegex)?.value
        ?: return Unmatched(parsingContext.position)

    return StringLiteral(stringLiteral, parsingContext.position)
}

val identifierRegex = Regex("([_a-zA-Z]+[_a-zA-Z0-9]*)")
private fun tryParseIdentifier(parsingContext: ParsingContext): Token {
    val identifier: String = firstGroup(parsingContext.remainingString, identifierRegex)?.value
        ?: return Unmatched(parsingContext.position)

    return Identifier(identifier, parsingContext.position)
}

@OptIn(ExperimentalStdlibApi::class)
private fun firstGroup(value: String, regex: Regex): MatchGroup? =
    regex.matchAt(value, 0)?.groups?.get(0)

