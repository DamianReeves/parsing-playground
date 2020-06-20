package parsing.playground.jsonnet

import fastparse._
import fastparse.MultiLineWhitespace._

object Parser {

  def parse(input: String): Parsed[Expr] =
    fastparse.parse(input, expr(_))

  def expr[_: P]: P[Expr] = P(prefixExpr ~ plus.rep).map {
    case (e, Nil)   => e
    case (e, items) => Expr.Plus(e +: items)
  }
  def prefixExpr[_: P]: P[Expr] = P(callExpr ~ call.rep).map {
    case (e, items) =>
      items.foldLeft(e)((f, args) => Expr.Call(f, args))
  }
  def callExpr[_: P] = P(str | dict | local | func | ident)

  def str[_: P] = P(str0).map(Expr.Str)
  def str0[_: P] = P("\"" ~~/ CharsWhile(_ != '"', 0).! ~~ "\"")
  def ident[_: P] =
    P(CharIn("a-zA-Z_") ~~ CharsWhileIn("a-zA-Z0-9_", 0)).!.map(Expr.Ident)
  def ident0[_: P] = P(CharIn("a-zA-Z_") ~~ CharsWhileIn("a-zA-Z0-9_", 0)).!

  def dict[_: P] =
    P("{" ~/ (str0 ~ ":" ~/ expr).rep(0, ",") ~ "}").map(kvs =>
      Expr.Dict(kvs.toMap)
    )
  def local[_: P] =
    P("local" ~/ ident0 ~ "=" ~ expr ~ ";" ~ expr).map(Expr.Local.tupled)
  def func[_: P] =
    P("function" ~/ "(" ~ ident0.rep(0, ",") ~ ")" ~ expr).map(Expr.Func.tupled)

  def plus[_: P] = P("+" ~ prefixExpr)
  def call[_: P] = P("(" ~/ expr.rep(0, ",") ~ ")")
}
