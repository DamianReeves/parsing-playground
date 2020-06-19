package parsing.playground.jsonnet

import fastparse._
import fastparse.MultiLineWhitespace._

object Parser {
  def expr[_: P]: P[Expr] = P(prefixExpr ~ plus.rep).map {
    case (e, Nil)   => e
    case (e, items) => Expr.Plus(e +: items)
  }
  def prefixExpr[_: P] = P(str | ident)

  def str[_: P] = P("\"" ~~/ CharsWhile(_ != '"', 0).! ~~ "\"").map(Expr.Str)
  def ident[_: P] =
    P(CharIn("a-zA-Z_") ~~ CharsWhileIn("a-zA-Z0-9_", 0)).!.map(Expr.Ident)
  def plus[_: P] = P("+" ~ prefixExpr)
}
