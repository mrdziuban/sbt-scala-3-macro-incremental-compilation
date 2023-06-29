package macros

import scala.deriving.Mirror
import scala.quoted.*

private class TypeMatchMacro(using val ctx: Quotes) {
  import ctx.reflect.*

  private def findMemberType(tpe: TypeRepr, name: String): Option[TypeRepr] =
    tpe match {
      case Refinement(_, `name`, t) => Some(Some(t).collect { case b: TypeBounds => b.low }.getOrElse(t))
      case Refinement(parent, _, _) => findMemberType(parent, name)
      case AndType(left, right) => findMemberType(left, name).orElse(findMemberType(right, name))
      case _ => None
    }

  private def unrollTuple(tpe: TypeRepr): List[TypeRepr] =
    tpe match {
      case AppliedType(_, List(h, t)) => h :: unrollTuple(t)
      case _ => Nil
    }

  private def getSubclasses(tpe: TypeRepr): List[TypeRepr] =
    tpe.asType match {
      case '[t] => Expr.summon[Mirror.Of[t]].fold(Nil) { m =>
        val mirrorTpe = m.asTerm.tpe
        findMemberType(mirrorTpe, "MirroredElemTypes").fold(Nil)(unrollTuple)
      }
    }

  def genTypeMatch[A: Type]: Expr[A => String] = {
    def ifBranches(a: Term): List[(Term, Term)] =
      getSubclasses(TypeRepr.of[A]).map { tpe =>
        val tpeSym = tpe.typeSymbol
        val cond = TypeApply(Select.unique(a, "isInstanceOf"), List(TypeIdent(tpeSym)))
        val action = Expr(tpe.select(tpeSym.typeMember("Type")).dealias.show).asTerm
        (cond, action)
      }

    def ifStatement(a: Term, branches: List[(Term, Term)]): Term =
      branches match {
        case (c, a) :: rest => If(c, a, ifStatement(a, rest))
        case Nil => '{ sys.error("Unexpected error") }.asTerm
      }

    '{ (a: A) => ${ ifStatement('a.asTerm, ifBranches('a.asTerm)).asExprOf[String] } }
  }
}

private def genTypeMatchImpl[A: Type](using ctx: Quotes): Expr[A => String] =
  new TypeMatchMacro().genTypeMatch[A]

inline def genTypeMatch[A]: A => String = ${ genTypeMatchImpl[A] }
