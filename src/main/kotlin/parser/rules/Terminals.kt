package parser.rules

import ast.BooleanLiteral
import ast.NilLiteral
import ast.NumericLiteral
import ast.StringLiteral
import ast.UnresolvedIdentifierExpression
import lexer.AndLexerToken
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.ClassLexerToken
import lexer.CommaLexerToken
import lexer.EOFLexerToken
import lexer.ElseLexerToken
import lexer.EqualEqualLexerToken
import lexer.EqualLexerToken
import lexer.FalseLexerToken
import lexer.ForLexerToken
import lexer.FunLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.IdentifierLexerToken
import lexer.IfLexerToken
import lexer.LeftBraceLexerToken
import lexer.LeftParenLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.MinusLexerToken
import lexer.NilLexerToken
import lexer.NumberLiteralLexerToken
import lexer.OrLexerToken
import lexer.PlusLexerToken
import lexer.PrintLexerToken
import lexer.ReturnLexerToken
import lexer.RightBraceLexerToken
import lexer.RightParenLexerToken
import lexer.SemicolonLexerToken
import lexer.SlashLexerToken
import lexer.StarLexerToken
import lexer.StringLiteralLexerToken
import lexer.TrueLexerToken
import lexer.VarLexerToken
import lexer.WhileLexerToken
import parser.Rule
import parser.nodeTokenRule
import parser.symbolicTokenRule

val identifierRule: Rule = nodeTokenRule<IdentifierLexerToken> { UnresolvedIdentifierExpression(it.name) }
val stringLiteralRule: Rule = nodeTokenRule<StringLiteralLexerToken> { StringLiteral(it.value) }
val numberLiteralRule: Rule = nodeTokenRule<NumberLiteralLexerToken> { NumericLiteral(it.value) }

val varKeywordRule: Rule = symbolicTokenRule<VarLexerToken>()
val funKeywordRule: Rule = symbolicTokenRule<FunLexerToken>()
val returnKeywordRule: Rule = symbolicTokenRule<ReturnLexerToken>()
val classKeywordRule: Rule = symbolicTokenRule<ClassLexerToken>()
val ifKeywordRule: Rule = symbolicTokenRule<IfLexerToken>()
val elseKeywordRule: Rule = symbolicTokenRule<ElseLexerToken>()
val orKeywordRule: Rule = symbolicTokenRule<OrLexerToken>()
val andKeywordRule: Rule = symbolicTokenRule<AndLexerToken>()
val whileKeywordRule: Rule = symbolicTokenRule<WhileLexerToken>()
val forKeywordRule: Rule = symbolicTokenRule<ForLexerToken>()
val printKeywordRule: Rule = symbolicTokenRule<PrintLexerToken>()

val trueRule: Rule = nodeTokenRule<TrueLexerToken> { BooleanLiteral(true) }
val falseRule: Rule = nodeTokenRule<FalseLexerToken> { BooleanLiteral(false) }
val nilRule: Rule = nodeTokenRule<NilLexerToken> { NilLiteral }

val commaRule: Rule = symbolicTokenRule<CommaLexerToken>()
val semicolonRule: Rule = symbolicTokenRule<SemicolonLexerToken>()

val leftBraceRule: Rule = symbolicTokenRule<LeftBraceLexerToken>()
val rightBraceRule: Rule = symbolicTokenRule<RightBraceLexerToken>()
val leftParenRule: Rule = symbolicTokenRule<LeftParenLexerToken>()
val rightParenRule: Rule = symbolicTokenRule<RightParenLexerToken>()

val bangRule: Rule = symbolicTokenRule<BangLexerToken>()
val plusRule: Rule = symbolicTokenRule<PlusLexerToken>()
val minusRule: Rule = symbolicTokenRule<MinusLexerToken>()
val starRule: Rule = symbolicTokenRule<StarLexerToken>()
val slashRule: Rule = symbolicTokenRule<SlashLexerToken>()

val greaterRule: Rule = symbolicTokenRule<GreaterLexerToken>()
val greaterEqualRule: Rule = symbolicTokenRule<GreaterEqualLexerToken>()
val lessRule: Rule = symbolicTokenRule<LessLexerToken>()
val lessEqualRule: Rule = symbolicTokenRule<LessEqualLexerToken>()

val equalRule: Rule = symbolicTokenRule<EqualLexerToken>()
val bangEqualRule: Rule = symbolicTokenRule<BangEqualLexerToken>()
val equalEqualRule: Rule = symbolicTokenRule<EqualEqualLexerToken>()

val eofRule: Rule = symbolicTokenRule<EOFLexerToken>()
