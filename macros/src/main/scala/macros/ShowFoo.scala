package macros

import types.*

def showFoo[A](f: Foo[A]): String = genTypeMatch[Foo[?]](f)
