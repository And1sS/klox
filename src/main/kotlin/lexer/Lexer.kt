package lexer

private val lineBreakRegex = Regex("\\r?\\n")
fun tokenize(source: String): List<LexerToken> =
    source.split(lineBreakRegex)
        .flatMapIndexed(::tokenizeLine)

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
        if (currentToken is EndOfLine) {
            return result
        } else if (currentToken is Unmatched) {
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

        else -> parseLiteral(withoutWhitespacesAndComments)
    }

    return Pair(token, withoutWhitespacesAndComments.skipChars(token.length))
}

private val whitespacesCommentsRegex = Regex("([\\s\\t]+(//.*)?)")
private fun skipWhitespacesAndComments(lexingContext: LexingContext): LexingContext {
    val matchEndIndex = firstMatch(lexingContext.remainingString, whitespacesCommentsRegex)?.length
        ?: return lexingContext

    return lexingContext.skipChars(matchEndIndex)
}

private val keywordsMap = mapOf<String, (Position) -> LexerToken>(
    "and" to ::And,
    "class" to ::Class,
    "else" to ::Else,
    "false" to ::False,
    "fun" to ::Fun,
    "for" to ::For,
    "if" to ::If,
    "nil" to ::Nil,
    "or" to ::Or,
    "print" to ::Print,
    "return" to ::Return,
    "super" to ::Super,
    "this" to ::This,
    "true" to ::True,
    "var" to ::Var,
    "while" to ::While,
)
private fun parseLiteral(lexingContext: LexingContext): LexerToken {
    val asNumberLiteral = tryParseNumberLiteral(lexingContext)
    if (asNumberLiteral is NumberLiteral) return asNumberLiteral

    val asStringLiteral = tryParseStringLiteral(lexingContext)
    if (asStringLiteral is StringLiteral) return asStringLiteral

    val asIdentifier = tryParseIdentifier(lexingContext)
    if (asIdentifier !is Identifier) return asIdentifier

    return keywordsMap[asIdentifier.name]?.invoke(lexingContext.position) ?: asIdentifier
}

private val numberLiteralRegex = Regex("(\\d+(?:\\.\\d+)?)")
private fun tryParseNumberLiteral(lexingContext: LexingContext): LexerToken {
    val numberLiteral: String = firstMatch(lexingContext.remainingString, numberLiteralRegex)
        ?: return Unmatched(lexingContext.position)

    return NumberLiteral(numberLiteral.toDouble(), numberLiteral.length, lexingContext.position)
}

private val stringLiteralRegex = Regex("\"([^\"]*)\"")
private fun tryParseStringLiteral(lexingContext: LexingContext): LexerToken {
    val stringLiteral: String = firstMatch(lexingContext.remainingString, stringLiteralRegex)
        ?: return Unmatched(lexingContext.position)

    return StringLiteral(stringLiteral, lexingContext.position)
}

private val identifierRegex = Regex("([_a-zA-Z]\\w*)")
private fun tryParseIdentifier(lexingContext: LexingContext): LexerToken {
    val identifier: String = firstMatch(lexingContext.remainingString, identifierRegex)
        ?: return Unmatched(lexingContext.position)

    return Identifier(identifier, lexingContext.position)
}

@OptIn(ExperimentalStdlibApi::class)
private fun firstMatch(value: String, regex: Regex): String? =
    regex.matchAt(value, 0)?.groups?.get(1)?.value