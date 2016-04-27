package scala.scalanative
package compiler
package pass

import nir._

/** Nrtoduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  override def preAssembly = { case defns =>
    val mainTy = Type.Function(Seq(Type.Module(entry.owner), Nrt.RefArray), Type.Void)
    val main   = Val.Global(entry, Type.Ptr(mainTy))
    val argc   = Val.Local(fresh(), Type.I32)
    val argv   = Val.Local(fresh(), Type.Ptr(Type.Ptr(Type.I8)))
    val module = Val.Local(fresh(), Type.Module(entry.owner))
    val arr    = Val.Local(fresh(), Nrt.RefArray)
    val sig    = Type.Function(Seq(argc.ty, argv.ty), Type.I32)

    defns :+
      Defn.Define(Seq(), Global.Val("main"), sig, Seq(
        Block(fresh(), Seq(argc, argv),
          Seq(Inst(arr.name, Nrt.call(Nrt.init, argc, argv)),
              Inst(module.name, Op.Module(entry.owner)),
              Inst(Op.Call(mainTy, main, Seq(module, arr)))),
          Cf.Ret(Val.I32(0)))))
  }
}