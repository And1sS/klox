package parser.rules

import lexer.AndLexerToken
import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EOFLexerToken
import lexer.ElseLexerToken
import lexer.EqualEqualLexerToken
import lexer.EqualLexerToken
import lexer.FalseLexerToken
import lexer.ForLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
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
import parser.ast.BooleanValue
import parser.ast.IdentifierExpression
import parser.ast.NilValue
import parser.ast.NumericValue
import parser.ast.StringValue
import parser.nodeTokenRule
import parser.symbolicTokenRule

val identifierRule: Rule = nodeTokenRule(::IdentifierExpression)
val stringLiteralRule: Rule = nodeTokenRule<StringLiteralLexerToken> { StringValue(it.value) }
val numberLiteralRule: Rule = nodeTokenRule<NumberLiteralLexerToken> { NumericValue(it.value) }

val ifKeywordRule: Rule = symbolicTokenRule<IfLexerToken>()
val elseKeywordRule: Rule = symbolicTokenRule<ElseLexerToken>()
val orKeywordRule: Rule = symbolicTokenRule<OrLexerToken>()
val andKeywordRule: Rule = symbolicTokenRule<AndLexerToken>()
val whileKeywordRule: Rule = symbolicTokenRule<WhileLexerToken>()
val forKeywordRule: Rule = symbolicTokenRule<ForLexerToken>()
val printKeywordRule: Rule = symbolicTokenRule<PrintLexerToken>()
val varKeywordRule: Rule = symbolicTokenRule<VarLexerToken>()

val trueRule: Rule = nodeTokenRule<TrueLexerToken> { BooleanValue(true) }
val falseRule: Rule = nodeTokenRule<FalseLexerToken> { BooleanValue(false) }
val nilRule: Rule = nodeTokenRule<NilLexerToken> { NilValue }

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
