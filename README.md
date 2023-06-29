# sbt + scala 3 macro incremental compilation issue

This reproduces an issue with incremental compilation in scala 3 when macros are involved.

There are two projects at play here:

- `types` -- just defines a sum type [`Foo`](types/src/main/scala/types/Foo.scala)
- `macros` -- has a few files:
  - [`TypeMatchMacro.scala`](macros/src/main/scala/macros/TypeMatchMacro.scala) defines a macro `genTypeMatch` that produces a function `A => String` for some `A` that's a sum type. The `String` is the name of `A`'s type member called `Type`
  - [`ShowFoo.scala`](macros/src/main/scala/macros/ShowFoo.scala) defines a function `showFoo` that shows a `Foo[A]` using the `genTypeMatch` macro
  - [`Main.scala`](macros/src/main/scala/macros/Main.scala) defines an entrypoint that calls `showFoo` with `Foo.Bar` and prints out the result

To reproduce the issue:

1. Clone this repo
2. `cd` into it
3. Run `sbt`
4. Run `macros/run`
    1. Note that it prints out `scala.Int`
5. Edit line 6 of [types/src/main/scala/types/Foo.scala](types/src/main/scala/types/Foo.scala) and change `Int` to `String`
6. Run `macros/run` again
    1. Note that it still prints out `scala.Int`

Interestingly, if you call `genTypeMatch[Foo[_]](Foo.Bar)` directly in `Main.scala` instead of going through `showFoo` in `ShowFoo.scala`, then the issue goes away.
