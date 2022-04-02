package lexer

data class Position(val line: Int, val position: Int) {
    fun skipChars(position: Int): Position =
        Position(line, position + this.position)
}

sealed class LexerToken(val position: Position, val length: Int)

class Unmatched(position: Position) : LexerToken(position, 0)
class EndOfLine(position: Position) : LexerToken(position, 0)

class LeftParen(position: Position) : LexerToken(position, 1)
class RightParen(position: Position) : LexerToken(position, 1)
class LeftBrace(position: Position) : LexerToken(position, 1)
class RightBrace(position: Position) : LexerToken(position, 1)
class Comma(position: Position) : LexerToken(position, 1)
class Dot(position: Position) : LexerToken(position, 1)
class Minus(position: Position) : LexerToken(position, 1)
class Plus(position: Position) : LexerToken(position, 1)
class Semicolon(position: Position) : LexerToken(position, 1)
class Slash(position: Position) : LexerToken(position, 1)
class Star(position: Position) : LexerToken(position, 1)

class Bang(position: Position) : LexerToken(position, 1)
class BangEqual(position: Position) : LexerToken(position, 2)
class Equal(position: Position) : LexerToken(position, 1)
class EqualEqual(position: Position) : LexerToken(position, 2)
class Greater(position: Position) : LexerToken(position, 1)
class GreaterEqual(position: Position) : LexerToken(position, 2)
class Less(position: Position) : LexerToken(position, 1)
class LessEqual(position: Position) : LexerToken(position, 2)

class Identifier(val name: String, position: Position) : LexerToken(position, name.length)
class StringLiteral(val value: String, position: Position) : LexerToken(position, value.length + 2) // Length with quotes
class NumberLiteral(val value: Double, length: Int, position: Position) : LexerToken(position, length)

class And(position: Position) : LexerToken(position, 3)
class Class(position: Position) : LexerToken(position, 5)
class Else(position: Position) : LexerToken(position, 4)
class False(position: Position) : LexerToken(position, 5)
class Fun(position: Position) : LexerToken(position, 3)
class For(position: Position) : LexerToken(position, 3)
class If(position: Position) : LexerToken(position, 2)
class Nil(position: Position) : LexerToken(position, 3)
class Or(position: Position) : LexerToken(position, 2)
class Print(position: Position) : LexerToken(position, 5)
class Return(position: Position) : LexerToken(position, 6)
class Super(position: Position) : LexerToken(position, 5)
class This(position: Position) : LexerToken(position, 4)
class True(position: Position) : LexerToken(position, 4)
class Var(position: Position) : LexerToken(position, 3)
class While(position: Position) : LexerToken(position, 5)
