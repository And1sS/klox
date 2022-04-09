package lexer

data class Position(val line: Int, val position: Int) {
    fun skipChars(position: Int): Position =
        Position(line, position + this.position)

    fun nextLine(): Position =
        Position(line + 1, 0)
}

sealed class LexerToken(val position: Position, val length: Int)

class UnmatchedLexerToken(position: Position) : LexerToken(position, 0)
class EOFLexerToken(position: Position) : LexerToken(position, 0)

class LeftParenLexerToken(position: Position) : LexerToken(position, 1)
class RightParenLexerToken(position: Position) : LexerToken(position, 1)
class LeftBraceLexerToken(position: Position) : LexerToken(position, 1)
class RightBraceLexerToken(position: Position) : LexerToken(position, 1)
class CommaLexerToken(position: Position) : LexerToken(position, 1)
class DotLexerToken(position: Position) : LexerToken(position, 1)
class MinusLexerToken(position: Position) : LexerToken(position, 1)
class PlusLexerToken(position: Position) : LexerToken(position, 1)
class SemicolonLexerToken(position: Position) : LexerToken(position, 1)
class SlashLexerToken(position: Position) : LexerToken(position, 1)
class StarLexerToken(position: Position) : LexerToken(position, 1)

class BangLexerToken(position: Position) : LexerToken(position, 1)
class BangEqualLexerToken(position: Position) : LexerToken(position, 2)
class EqualLexerToken(position: Position) : LexerToken(position, 1)
class EqualEqualLexerToken(position: Position) : LexerToken(position, 2)
class GreaterLexerToken(position: Position) : LexerToken(position, 1)
class GreaterEqualLexerToken(position: Position) : LexerToken(position, 2)
class LessLexerToken(position: Position) : LexerToken(position, 1)
class LessEqualLexerToken(position: Position) : LexerToken(position, 2)

class IdentifierLexerToken(val name: String, position: Position) : LexerToken(position, name.length)
class StringLiteralLexerToken(val value: String, position: Position) :
    LexerToken(position, value.length + 2) // Length with quotes

class NumberLiteralLexerToken(val value: Double, length: Int, position: Position) : LexerToken(position, length)

class AndLexerToken(position: Position) : LexerToken(position, 3)
class ClassLexerToken(position: Position) : LexerToken(position, 5)
class ElseLexerToken(position: Position) : LexerToken(position, 4)
class FalseLexerToken(position: Position) : LexerToken(position, 5)
class FunLexerToken(position: Position) : LexerToken(position, 3)
class ForLexerToken(position: Position) : LexerToken(position, 3)
class IfLexerToken(position: Position) : LexerToken(position, 2)
class NilLexerToken(position: Position) : LexerToken(position, 3)
class OrLexerToken(position: Position) : LexerToken(position, 2)
class PrintLexerToken(position: Position) : LexerToken(position, 5)
class ReturnLexerToken(position: Position) : LexerToken(position, 6)
class SuperLexerToken(position: Position) : LexerToken(position, 5)
class ThisLexerToken(position: Position) : LexerToken(position, 4)
class TrueLexerToken(position: Position) : LexerToken(position, 4)
class VarLexerToken(position: Position) : LexerToken(position, 3)
class WhileLexerToken(position: Position) : LexerToken(position, 5)
