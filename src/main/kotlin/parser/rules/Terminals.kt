package parser.rules

import lexer.BangEqualLexerToken
import lexer.BangLexerToken
import lexer.EOFLexerToken
import lexer.EqualEqualLexerToken
import lexer.EqualLexerToken
import lexer.FalseLexerToken
import lexer.GreaterEqualLexerToken
import lexer.GreaterLexerToken
import lexer.LeftBraceLexerToken
import lexer.LeftParenLexerToken
import lexer.LessEqualLexerToken
import lexer.LessLexerToken
import lexer.MinusLexerToken
import lexer.NilLexerToken
import lexer.NumberLiteralLexerToken
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
import parser.ast.BooleanValue
import parser.ast.IdentifierExpression
import parser.ast.NilValue
import parser.ast.NumericValue
import parser.Rule
import parser.ast.StringValue
import parser.nodeTokenRule
import parser.symbolicTokenRule

val identifierRule = nodeTokenRule(::IdentifierExpression)
val stringLiteralRule = nodeTokenRule<StringLiteralLexerToken> { StringValue(it.value) }
val numberLiteralRule = nodeTokenRule<NumberLiteralLexerToken> { NumericValue(it.value) }

val printRule = symbolicTokenRule<PrintLexerToken>()
val varRule = symbolicTokenRule<VarLexerToken>()

val trueRule = nodeTokenRule<TrueLexerToken> { BooleanValue(true) }
val falseRule = nodeTokenRule<FalseLexerToken> { BooleanValue(false) }
val nilRule = nodeTokenRule<NilLexerToken> { NilValue }

val semicolonRule = symbolicTokenRule<SemicolonLexerToken>()

val leftBraceRule = symbolicTokenRule<LeftBraceLexerToken>()
val rightBraceRule = symbolicTokenRule<RightBraceLexerToken>()
val leftParenRule = symbolicTokenRule<LeftParenLexerToken>()
val rightParenRule = symbolicTokenRule<RightParenLexerToken>()

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