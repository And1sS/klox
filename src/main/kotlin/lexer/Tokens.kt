package lexer

data class Position(val line: Int, val position: Int) {

    fun skipChars(position: Int): Position =
        Position(line, position + this.position)
}

sealed class Token(val position: Position, val length: Int)

class Unmatched(position: Position) : Token(position, 0)
class EndOfLine(position: Position) : Token(position, 0)

class LeftParen(position: Position) : Token(position, 1)
class RightParen(position: Position) : Token(position, 1)
class LeftBrace(position: Position) : Token(position, 1)
class RightBrace(position: Position) : Token(position, 1)
class Comma(position: Position) : Token(position, 1)
class Dot(position: Position) : Token(position, 1)
class Minus(position: Position) : Token(position, 1)
class Plus(position: Position) : Token(position, 1)
class Semicolon(position: Position) : Token(position, 1)
class Slash(position: Position) : Token(position, 1)
class Star(position: Position) : Token(position, 1)

class Bang(position: Position) : Token(position, 1)
class BangEqual(position: Position) : Token(position, 2)
class Equal(position: Position) : Token(position, 1)
class EqualEqual(position: Position) : Token(position, 2)
class Greater(position: Position) : Token(position, 1)
class GreaterEqual(position: Position) : Token(position, 2)
class Less(position: Position) : Token(position, 1)
class LessEqual(position: Position) : Token(position, 2)

class Identifier(val name: String, position: Position) : Token(position, name.length)
class StringLiteral(val value: String, position: Position) : Token(position, value.length)
class NumberLiteral(val value: Double, length: Int, position: Position) : Token(position, length)

class And(position: Position) : Token(position, 3)
class Class(position: Position) : Token(position, 5)
class Else(position: Position) : Token(position, 4)
class False(position: Position) : Token(position, 5)
class Fun(position: Position) : Token(position, 3)
class For(position: Position) : Token(position, 3)
class If(position: Position) : Token(position, 2)
class Nil(position: Position) : Token(position, 3)
class Or(position: Position) : Token(position, 2)
class Print(position: Position) : Token(position, 5)
class Return(position: Position) : Token(position, 6)
class Super(position: Position) : Token(position, 5)
class This(position: Position) : Token(position, 4)
class True(position: Position) : Token(position, 4)
class Var(position: Position) : Token(position, 3)
class While(position: Position) : Token(position, 5)
