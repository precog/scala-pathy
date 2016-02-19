/*
 * Copyright 2014 - 2015 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pathy

import scala.Function.const

import scala.annotation.tailrec
import scalaz._, Tags.Conjunction
import scalaz.std.anyVal._
import scalaz.std.option._
import scalaz.std.string._
import scalaz.syntax.either._
import scalaz.syntax.equal._
import scalaz.syntax.monoid._
import scalaz.syntax.show._

sealed abstract class Path2[B <: Path2.Base, T <: Path2.Typ] {
  override def toString = this.shows
}

object Path2 {
  sealed trait Base
  sealed trait Abs extends Base
  sealed trait Rel extends Base

  sealed trait Typ
  sealed trait File extends Typ
  sealed trait Dir extends Typ

  type RPath[T <: Typ] = Path2[Rel, T]
  type APath[T <: Typ] = Path2[Abs, T]

  type RFile = RPath[File]
  type AFile = APath[File]

  type RDir = RPath[Dir]
  type ADir = APath[Dir]

  final case class FileName(value: String) extends AnyVal {
    def dropExtension: FileName = {
      val idx = value.lastIndexOf(".")
      if (idx == -1) this else FileName(value.substring(0, idx))
    }

    def extension: String = {
      val idx = value.lastIndexOf(".")
      if (idx == -1) "" else value.substring(idx + 1)
    }

    def modifyExtension(f: String => String): FileName =
      FileName(dropExtension.value + "." + f(extension))
  }

  final case class DirName(value: String) extends AnyVal

  private final case object Root
    extends Path2[Abs, Dir]
  private final case object Current
    extends Path2[Rel, Dir]
  private final case class ParentIn(parent: Path2[Rel, Dir])
    extends Path2[Rel, Dir]
  private final case class DirIn[B <: Base](parent: Path2[B, Dir], name: DirName)
    extends Path2[B, Dir]
  private final case class FileIn[B <: Base](parent: Path2[B, Dir], name: FileName)
    extends Path2[B, File]

  //--- Constructors ---

  def file(name: String): RFile =
    file1(FileName(name))

  def file1(name: FileName): RFile =
    FileIn(currentDir, name)

  def dir(name: String): RDir =
    dir1(DirName(name))

  def dir1(name: DirName): RDir =
    DirIn(currentDir, name)

  val currentDir: Path2[Rel, Dir] = Current

  val rootDir: Path2[Abs, Dir] = Root

  //--- Combinators ---

  def append[B <: Base, T <: Typ](d: Path2[B, Dir], p: RPath[T]): Path2[B, T] =
    p match {
      case FileIn(pp, n) => FileIn(append(d, pp), n)
      case DirIn(pp, n)  => DirIn(append(d, pp), n)
      case ParentIn(pp)  => append(parentDir(d), pp)
      case Current       => d
    }

  def asRelative[T <: Typ]: APath[T] => RPath[T] = {
    case FileIn(p, n) => FileIn(asRelative(p), n)
    case DirIn(p, n)  => DirIn(asRelative(p), n)
    case Root         => currentDir
  }

  val depth: Path2[_, _] => Int =
    foldMap(const(1), const(1), -1, _)

  val dirName: Path2[_, Dir] => Option[DirName] = {
    case DirIn(_, n) => some(n)
    case ParentIn(_) => none
    case Current     => none
    case Root        => none
  }

  val fileName: Path2[_, File] => FileName = {
    case FileIn(_, n) => n
  }

  def foldMap1[A: Semigroup](
    fileIn: FileName => A,
    dirIn: DirName => A,
    parentIn: => A,
    cur: => A,
    root: => A,
    path: Path2[_, _]
  ): A = {
    @tailrec
    def go(pth: Path2[_, _], a: A): A = pth match {
      case FileIn(p, n) => go(p, fileIn(n) |+| a)
      case DirIn(p, n)  => go(p, dirIn(n) |+| a)
      case ParentIn(p)  => go(p, parentIn |+| a)
      case Current      => cur |+| a
      case Root         => root |+| a
    }

    path match {
      case FileIn(p, n) => go(p, fileIn(n))
      case DirIn(p, n)  => go(p, dirIn(n))
      case ParentIn(p)  => go(p, parentIn)
      case Current      => cur
      case Root         => root
    }
  }

  def foldMap[A: Monoid](
    fileIn: FileName => A,
    dirIn: DirName => A,
    parentIn: => A,
    path: Path2[_, _]
  ): A =
    foldMap1(fileIn, dirIn, parentIn, mzero[A], mzero[A], path)

  def foldMapA1[A: Semigroup](
    fileIn: FileName => A,
    dirIn: DirName => A,
    root: A,
    path: APath[_]
  ): A = {
    @tailrec
    def go(ap: APath[_], a: A): A = ap match {
      case FileIn(p, n) => go(p, fileIn(n) |+| a)
      case DirIn(p, n)  => go(p, dirIn(n) |+| a)
      case Root         => root |+| a
    }

    path match {
      case FileIn(p, n) => go(p, fileIn(n))
      case DirIn(p, n)  => go(p, dirIn(n))
      case Root         => root
    }
  }

  def foldMapA[A: Monoid](
    fileIn: FileName => A,
    dirIn: DirName => A,
    path: APath[_]
  ): A =
    foldMapA1(fileIn, dirIn, mzero[A], path)

  def foldMapR1[A: Semigroup](
    fileIn: FileName => A,
    dirIn: DirName => A,
    parentIn: => A,
    cur: A,
    path: RPath[_]
  ): A = {
    @tailrec
    def go(rp: RPath[_], a: A): A = rp match {
      case FileIn(p, n) => go(p, fileIn(n) |+| a)
      case DirIn(p, n)  => go(p, dirIn(n) |+| a)
      case ParentIn(p)  => go(p, parentIn |+| a)
      case Current      => cur |+| a
    }

    path match {
      case FileIn(p, n) => go(p, fileIn(n))
      case DirIn(p, n)  => go(p, dirIn(n))
      case ParentIn(p)  => go(p, parentIn)
      case Current      => cur
    }
  }

  def foldMapR[A: Monoid](
    fileIn: FileName => A,
    dirIn: DirName => A,
    parentIn: => A,
    path: RPath[_]
  ): A =
    foldMapR1(fileIn, dirIn, parentIn, mzero[A], path)

  val isAbsolute: Path2[_, _] => Boolean =
    (foldMap1(
      const(Conjunction(true)),
      const(Conjunction(true)),
      Conjunction(false),
      Conjunction(false),
      Conjunction(true),
      _: Path2[_, _]
    )) andThen Conjunction.unwrap

  val isRelative: Path2[_, _] => Boolean =
    isAbsolute andThen (!_)

  def maybeDir[B <: Base]: Path2[B, _] => Option[Path2[B, Dir]] =
    refineType(_).swap.toOption

  def maybeFile[B <: Base]: Path2[B, _] => Option[Path2[B, File]] =
    refineType(_).toOption

  def parentDir[B <: Base]: Path2[B, _] => Path2[B, Dir] = {
    case FileIn(p, _) => p
    case DirIn(p, _)  => p
    case ParentIn(p)  => ParentIn(ParentIn(p))
    case Current      => ParentIn(Current)
    case Root         => Root
  }

  def peel[B <: Base]: Path2[B, _] => Option[(Path2[B, Dir], DirName \/ FileName)] = {
    case FileIn(d, n) => some((d, n.right))
    case DirIn(d, n)  => some((d, n.left))
    case ParentIn(d)  => none
    case Current      => none
    case Root         => none
  }

  def refineType[B <: Base]: Path2[B, _] => Path2[B, Dir] \/ Path2[B, File] = {
    case FileIn(d, n) => FileIn(d, n).right
    case DirIn(d, n)  => DirIn(d, n).left
    case ParentIn(d)  => ParentIn(d).left
    case Current      => currentDir.left
    case Root         => rootDir.left
  }

  def relativeTo[B <: Base, T <: Typ](p: Path2[B, T], d: Path2[B, Dir]): Option[RPath[T]] = {
    def go(d1: Path2[B, Dir], d2: Path2[B, Dir]): Option[RDir] =
      if (d1 === d2)
        some(currentDir)
      else d1 match {
        case DirIn(pp, n)  => go(pp, d2) map (append(_, dir1(n)))
        case _             => none
      }

    p match {
      case FileIn(pp, n)    => go(pp, d) map (append(_, file1(n)))
      case dp @ DirIn(_, _) => go(dp, d)
      case dp @ ParentIn(_) => go(dp, d)
      case dp @ Current     => go(dp, d)
      case dp @ Root        => go(dp, d)
    }
  }

  def renameDir[B <: Base](f: DirName => DirName): Path2[B, Dir] => Path2[B, Dir] = {
    case DirIn(d, n)     => DirIn(d, f(n))
    case p @ ParentIn(_) => p
    case c @ Current     => c
    case r @ Root        => r
  }

  def renameFile[B <: Base](f: FileName => FileName): Path2[B, File] => Path2[B, File] = {
    case FileIn(d, n) => FileIn(d, f(n))
  }

  //--- Syntax ---

  final implicit class PathOps[B <: Base, T <: Typ](val p: Path2[B, T]) extends AnyVal {
    def asRelative(implicit ev: Leibniz[Nothing, Base, B, Abs]): RPath[T] =
      Path2.asRelative(ev.subst[({type f[x <: Base] = Path2[x, T]})#f](p))

    def depth: Int =
      Path2.depth(p)

    def foldMap1[A: Semigroup](
      fileIn: FileName => A,
      dirIn: DirName => A,
      parentIn: => A,
      cur: => A,
      root: => A
    ): A =
      Path2.foldMap1(fileIn, dirIn, parentIn, cur, root, p)

    def foldMap[A: Monoid](
      fileIn: FileName => A,
      dirIn: DirName => A,
      parentIn: => A
    ): A =
      Path2.foldMap(fileIn, dirIn, parentIn, p)

    def foldMapA1[A: Semigroup](
      fileIn: FileName => A,
      dirIn: DirName => A,
      root: A
    )(implicit ev: Leibniz[Nothing, Base, B, Abs]): A =
      Path2.foldMapA1(fileIn, dirIn, root, ev.subst[({type f[x <: Base] = Path2[x, T]})#f](p))

    def foldMapA[A: Monoid](
      fileIn: FileName => A,
      dirIn: DirName => A
    )(implicit ev: Leibniz[Nothing, Base, B, Abs]): A =
      Path2.foldMapA(fileIn, dirIn, ev.subst[({type f[x <: Base] = Path2[x, T]})#f](p))

    def foldMapR1[A: Semigroup](
      fileIn: FileName => A,
      dirIn: DirName => A,
      parentIn: => A,
      cur: A
    )(implicit ev: Leibniz[Nothing, Base, B, Rel]): A =
      Path2.foldMapR1(fileIn, dirIn, parentIn, cur, ev.subst[({type f[x <: Base] = Path2[x, T]})#f](p))

    def foldMapR[A: Monoid](
      fileIn: FileName => A,
      dirIn: DirName => A,
      parentIn: => A
    )(implicit ev: Leibniz[Nothing, Base, B, Rel]): A =
      Path2.foldMapR(fileIn, dirIn, parentIn, ev.subst[({type f[x <: Base] = Path2[x, T]})#f](p))

    def isAbsolute: Boolean =
      Path2.isAbsolute(p)

    def isRelative: Boolean =
      Path2.isRelative(p)

    def maybeDir: Option[Path2[B, Dir]] =
      Path2.maybeDir(p)

    def maybeFile: Option[Path2[B, File]] =
      Path2.maybeFile(p)

    def parentDir: Path2[B, Dir] =
      Path2.parentDir(p)

    def peel: Option[(Path2[B, Dir], DirName \/ FileName)] =
      Path2.peel(p)

    def refineType: Path2[B, Dir] \/ Path2[B, File] =
      Path2.refineType(p)

    def relativeTo(d: Path2[B, Dir]): Option[RPath[T]] =
      Path2.relativeTo(p, d)
  }

  final implicit class FileOps[B <: Base](val p: Path2[B, File]) extends AnyVal {
    def name: FileName =
      Path2.fileName(p)

    def rename(f: FileName => FileName): Path2[B, File] =
      Path2.renameFile(f)(p)
  }

  final implicit class DirOps[B <: Base](val p: Path2[B, Dir]) extends AnyVal {
    def /[T <: Typ](r: RPath[T]): Path2[B, T] =
      Path2.append(p, r)

    def /(n: String): Path2[B, Dir] =
      p / dir(n)

    def name: Option[DirName] =
      Path2.dirName(p)

    def rename(f: DirName => DirName): Path2[B, Dir] =
      Path2.renameDir(f)(p)
  }

  //--- Instances ---

  implicit def pathEqual[B <: Base, T <: Typ]: Equal[Path2[B, T]] =
    Equal.equalA

  // TODO: Replace w/posix codec
  implicit def pathShow[B <: Base, T <: Typ]: Show[Path2[B, T]] =
    Show.shows(foldMap1(_.value, _.value + "/", "../", "./", "/", _))
}
