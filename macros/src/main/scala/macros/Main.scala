package macros

import types.*

object Main {
  def main(args: Array[String]): Unit =
    println(showFoo(Foo.Bar))
}
