package types

sealed trait Foo[A] { final type Type = A }

object Foo {
  case object Bar extends Foo[String]
}
