package parsing.playground.jsonnet

import parsing.playground.jsonnet.testing.ParsingSpec
import zio.test._
import zio.test.Assertion._

object EvaluatorSpec extends DefaultRunnableSpec with ParsingSpec {
  import Evaluator.default.evaluate
  import Parser.expr
  import Value._

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
            Parser
              .parse(
                """local greeting = "Hello "; nope + nope"""
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
      ),
      test(
        "It should support function call evaluations containing indirections"
      )(
        assert(
          evaluate(
            fastparse
              .parse(
                """local f = function(a, b) a + " " + b; f("hello", "world")""",
                expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(equalTo(Value.Str("hello world")))
      ),
      test("It should support function call evaluation")(
        assert(
          evaluate(
            fastparse
              .parse(
                """local hello = function(name) "Hello " + name; hello("Bob")""",
                expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(equalTo(Value.Str("Hello Bob")))
      ),
      test("It should support a full breadth of features")(
        assert(
          evaluate(
            fastparse
              .parse(
                """local greeting = "Hello ";
         local person = function (name) {
           "name": name,
           "welcome": greeting + name + "!"
         };
         {
           "person1": person("Alice"),
           "person2": person("Bob"),
           "person3": person("Charlie")
         }
      """,
                expr(_)
              )
              .get
              .value,
            Map.empty
          )
        )(
          equalTo(
            Value.Dict(
              Map(
                "person1" -> Dict(
                  Map("name" -> Str("Alice"), "welcome" -> Str("Hello Alice!"))
                ),
                "person2" -> Dict(
                  Map("name" -> Str("Bob"), "welcome" -> Str("Hello Bob!"))
                ),
                "person3" -> Dict(
                  Map(
                    "name" -> Str("Charlie"),
                    "welcome" -> Str("Hello Charlie!")
                  )
                )
              )
            )
          )
        )
      )
    )
  )
}
