package parsing.playground.jsonnet.testing

import fastparse._
import zio.test.Assertion
import fastparse.Parsed.Failure
import fastparse.Parsed.Success

trait ParsingSpec {
  object AssertThat {
    import zio.test.Assertion.Render._

    def hasParsedSuccessValue[T](
        assertion: Assertion[T]
    ): Assertion[Parsed[T]] = {
      def get(parsed: => Parsed[T]): Option[T] = parsed match {
        case _: Failure        => None
        case Success(value, _) => Some(value)
      }
      Assertion.assertionRec[Parsed[T], T](
        "hasParsedSuccessValue"
      )(
        param(assertion)
      )(
        assertion
      )(get)
    }

    def isParseSuccess[T](
        assertion: Assertion[Parsed.Success[T]]
    ): Assertion[Parsed[T]] = {
      def get(parsed: => Parsed[T]): Option[Parsed.Success[T]] = parsed match {
        case _: Failure              => None
        case success @ Success(_, _) => Some(success)
      }
      Assertion.assertionRec[Parsed[T], Parsed.Success[T]](
        "isParseSuccess"
      )(
        param(assertion)
      )(
        assertion
      )(get)
    }

    val isParseSuccess: Assertion[Parsed[_]] =
      Assertion.assertion[Parsed[_]]("isParseSuccess")() {
        case Parsed.Success(_, _)    => true
        case Parsed.Failure(_, _, _) => false
      }

    val isParseFailure: Assertion[Parsed[_]] =
      Assertion.assertion[Parsed[_]]("isParseFailure")() {
        case Parsed.Success(_, _)    => false
        case Parsed.Failure(_, _, _) => true
      }
    //def isParsedToValueEqualTo[A](expected:A) = Assertion.assertion("isParseSuccess")(param(expected))(actual )
  }
}
