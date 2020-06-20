package parsing.playground.jsonnet

import parsing.playground.jsonnet.testing.ParsingSpec
import zio.test._
import zio.test.Assertion._

object EvaluatorSpec extends DefaultRunnableSpec with ParsingSpec {
  import Evaluator.default.evaluate
  def spec = suite("Evaluator Spec")(
    suite("evaluate")(
      test("It should work for a simple Str literal") {
        val parsed = fastparse.parse("\"hello\"", Parser.expr(_))
        val expr = parsed.get.value
        assert(Evaluator.default.evaluate(expr, Map.empty))(
          equalTo(Value.Str("hello"))
        )
      },
      test("It should work for a Dict of Str literals")(
        assert(
          Evaluator.default.evaluate(
            fastparse
              .parse("""{"hello": "world", "key": "value"}""", Parser.expr(_))
              .get
              .value,
            Map.empty
          )
        )(
          equalTo(
            Value.Dict(
              Map("hello" -> Value.Str("world"), "key" -> Value.Str("value"))
            )
          )
        )
      ),
      test("It should support string concatenation using plus")(
        assert(
          evaluate(
            fastparse.parse("\"hello\" + \"world\"", Parser.expr(_)).get.value,
            Map.empty
          )
        )(equalTo(Value.Str("helloworld")))
      ),
      test("It should support local variable assignment and usage")(
        assert(
          evaluate(
            fastparse
              .parse(
                """local greeting = "Hello "; greeting + greeting""",
                Parser.expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(
          equalTo(Value.Str("Hello Hello "))
        )
      ),
      test(
        "It should support multiple chained local variables assignment and usage"
      )(
        assert(
          evaluate(
            fastparse
              .parse(
                """local x = "Hello "; local y = "world"; x + y""",
                Parser.expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(
          equalTo(Value.Str("Hello world"))
        )
      ),
      test("It should fail when a local variable is not found")(
        assert(
          evaluate(
            fastparse
              .parse(
                """local greeting = "Hello "; nope + nope""",
                Parser.expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(
          throwsA[NoSuchElementException] && throws(
            hasMessage(startsWithString("key not found: nope"))
          )
        )
      )
    )
  )
}
