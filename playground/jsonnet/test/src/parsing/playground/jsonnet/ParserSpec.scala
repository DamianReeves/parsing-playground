package parsing.playground.jsonnet

import zio.test._
import zio.test.Assertion._
import parsing.playground.jsonnet.testing._

object ParserSpec extends DefaultRunnableSpec with ParsingSpec {
  def spec = suite("Parser Spec")(
    suite("Parsers:")(
      suite("str")(
        testM("Should successfully parse")(
          check(Gen.fromIterable(Seq("\"hello\"", "\"hello world\"", "\"\""))) {
            input =>
              assert(fastparse.parse(input, Parser.str(_)))(
                AssertThat.isParseSuccess
              )
          }
        ),
        testM("Should fail to parse")(
          check(Gen.fromIterable(Seq("no quotes"))) { input =>
            assert(fastparse.parse(input, Parser.str(_)))(
              AssertThat.isParseFailure
            )
          }
        )
      ),
      suite("ident")(
        testM("Should successfully parse")(
          check(Gen.fromIterable(Seq("hello", "andre3000"))) { input =>
            assert(fastparse.parse(input, Parser.ident(_)))(
              AssertThat.isParseSuccess
            )
          }
        ),
        testM("Should fail to parse")(
          check(Gen.fromIterable(Seq("123", "no spaces"))) { input =>
            assert(fastparse.parse(input, Parser.str(_)))(
              AssertThat.isParseFailure
            )
          }
        )
      ),
      suite("expr")(
        test("A simple binary addition should parse successfully")(
          assert(fastparse.parse("a + b", Parser.expr(_)))(
            AssertThat.hasParsedSuccessValue(
              equalTo(
                Expr.Plus(
                  Seq(Expr.Ident("a"), Expr.Ident("b"))
                )
              )
            )
          )
        ),
        test("A simple addition should parse successfully")(
          assert(fastparse.parse("a + b + c", Parser.expr(_)))(
            AssertThat.hasParsedSuccessValue(
              equalTo(
                Expr.Plus(
                  Seq(Expr.Ident("a"), Expr.Ident("b"), Expr.Ident("c"))
                )
              )
            )
          )
        )
      )
    )
  )
}
