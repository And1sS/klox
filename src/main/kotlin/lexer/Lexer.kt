package lexer

private val lineBreakRegex = Regex("\\r?\\n")
fun String.tokenize(): List<LexerToken> =
    this.split(lineBreakRegex)
        .flatMapIndexed(::tokenizeLine)
        .let { it.plusElement(EOFLexerToken(it.last().position.nextLine())) }

private data class LexingContext(
    val remainingString: String,
    val position: Position
) {
    fun skipChars(amount: Int): LexingContext =
        LexingContext(
            remainingString.drop(amount),
            position.skipChars(amount)
        )
}

private fun tokenizeLine(lineNumber: Int, line: String): List<LexerToken> {
    var currentContext = LexingContext(line, Position(lineNumber, 0))
    val result = mutableListOf<LexerToken>()

    while (true) {
        val (currentToken, nextContext) = nextToken(currentContext)
        if (currentToken is EOFLexerToken) {
            return result
        } else if (currentToken is UnmatchedLexerToken) {
            throw RuntimeException("Unmatched token on position: ${currentToken.position}")
        }

        result.add(currentToken)
        currentContext = nextContext
    }
}

private fun nextToken(lexingContext: LexingContext): Pair<LexerToken, LexingContext> {
    val withoutWhitespacesAndComments = skipWhitespacesAndComments(lexingContext)
    val (source, position) = withoutWhitespacesAndComments

    val token: LexerToken = when {
        source.isEmpty() -> EOFLexerToken(position)
        source.startsWith("(") -> LeftParenLexerToken(position)
        source.startsWith(")") -> RightParenLexerToken(position)
        source.startsWith("{") -> LeftBraceLexerToken(position)
        source.startsWith("}") -> RightBraceLexerToken(position)
        source.startsWith(",") -> CommaLexerToken(position)
        source.startsWith(".") -> DotLexerToken(position)
        source.startsWith("-") -> MinusLexerToken(position)
        source.startsWith("+") -> PlusLexerToken(position)
        source.startsWith(";") -> SemicolonLexerToken(position)
        source.startsWith("/") -> SlashLexerToken(position)
        source.startsWith("*") -> StarLexerToken(position)

        source.startsWith("!=") -> BangEqualLexerToken(position)
        source.startsWith("==") -> EqualEqualLexerToken(position)
        source.startsWith(">=") -> GreaterEqualLexerToken(position)
        source.startsWith("<=") -> LessEqualLexerToken(position)

        source.startsWith("!") -> BangLexerToken(position)
        source.startsWith("=") -> EqualLexerToken(position)
        source.startsWith(">") -> GreaterLexerToken(position)
        source.startsWith("<") -> LessLexerToken(position)

        else -> parseLiteral(withoutWhitespacesAndComments)
    }

    return Pair(token, withoutWhitespacesAndComments.skipChars(token.length))
}

private val whitespacesCommentsRegex = Regex("([\\s\\t]*(//.*)?)")
private fun skipWhitespacesAndComments(lexingContext: LexingContext): LexingContext {
    val matchEndIndex = firstMatch(lexingContext.remainingString, whitespacesCommentsRegex)?.length
        ?: return lexingContext

    return lexingContext.skipChars(matchEndIndex)
}

private val keywordsMap = mapOf<String, (Position) -> LexerToken>(
    "and" to ::AndLexerToken,
    "class" to ::ClassLexerToken,
    "else" to ::ElseLexerToken,
    "false" to ::FalseLexerToken,
    "fun" to ::FunLexerToken,
    "for" to ::ForLexerToken,
    "if" to ::IfLexerToken,
    "nil" to ::NilLexerToken,
    "or" to ::OrLexerToken,
    "print" to ::PrintLexerToken,
    "return" to ::ReturnLexerToken,
    "super" to ::SuperLexerToken,
    "this" to ::ThisLexerToken,
    "true" to ::TrueLexerToken,
    "var" to ::VarLexerToken,
    "while" to ::WhileLexerToken,
)

private fun parseLiteral(lexingContext: LexingContext): LexerToken {
    val asNumberLiteral = tryParseNumberLiteral(lexingContext)
    if (asNumberLiteral is NumberLiteralLexerToken) return asNumberLiteral

    val asStringLiteral = tryParseStringLiteral(lexingContext)
    if (asStringLiteral is StringLiteralLexerToken) return asStringLiteral

    val asIdentifier = tryParseIdentifier(lexingContext)
    if (asIdentifier !is IdentifierLexerToken) return asIdentifier

    return keywordsMap[asIdentifier.name]?.invoke(lexingContext.position) ?: asIdentifier
}

private val numberLiteralRegex = Regex("(\\d+(?:\\.\\d+)?)")
private fun tryParseNumberLiteral(lexingContext: LexingContext): LexerToken {
    val numberLiteral: String = firstMatch(lexingContext.remainingString, numberLiteralRegex)
        ?: return UnmatchedLexerToken(lexingContext.position)

    return NumberLiteralLexerToken(numberLiteral.toDouble(), numberLiteral.length, lexingContext.position)
}

private val stringLiteralRegex = Regex("\"([^\"]*)\"")
private fun tryParseStringLiteral(lexingContext: LexingContext): LexerToken {
    val stringLiteral: String = firstMatch(lexingContext.remainingString, stringLiteralRegex)
        ?: return UnmatchedLexerToken(lexingContext.position)

    return StringLiteralLexerToken(stringLiteral, lexingContext.position)
}

private val identifierRegex = Regex("([_a-zA-Z]\\w*)")
private fun tryParseIdentifier(lexingContext: LexingContext): LexerToken {
    val identifier: String = firstMatch(lexingContext.remainingString, identifierRegex)
        ?: return UnmatchedLexerToken(lexingContext.position)

    return IdentifierLexerToken(identifier, lexingContext.position)
}

private fun firstMatch(value: String, regex: Regex): String? =
    regex.matchAt(value, 0)?.groups?.get(1)?.value