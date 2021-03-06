/*
 * Copyright 2020 Precog Data
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

import slamdata.Predef._

import scala.annotation.tailrec
import scala.sys

import scalaz._, Scalaz._

sealed trait Path[+B,+T,+S] {
  def isAbsolute: Boolean
  def isRelative: Boolean = !isAbsolute
}

object Path {
  sealed trait Rel
  sealed trait Abs

  sealed trait File
  sealed trait Dir

  sealed trait Sandboxed
  sealed trait Unsandboxed

  final case class FileName(value: String) extends AnyVal {
    def extension: String = {
      val idx = value.lastIndexOf(".")
      if (idx ≟ -1) "" else value.substring(idx + 1)
    }

    def dropExtension: FileName = {
      val idx = value.lastIndexOf(".")
      if (idx ≟ -1) this else FileName(value.substring(0, idx))
    }

    def changeExtension(f: String => String): FileName =
      FileName(dropExtension.value + "." + f(extension))
  }

  object FileName {
    implicit val order: Order[FileName] =
      Order.orderBy(_.value)

    implicit val show: Show[FileName] =
      Show.shows(_.value)
  }

  final case class DirName(value: String) extends AnyVal

  object DirName {
    implicit val order: Order[DirName] =
      Order.orderBy(_.value)

    implicit val show: Show[DirName] =
      Show.shows(_.value)
  }

  // Note: this ADT allows invalid paths, but the exposed functions
  // of the package do not.
  private case object Current extends Path[Nothing,Nothing,Nothing] {
    def isAbsolute = false
  }
  private case object Root extends Path[Nothing,Nothing,Nothing] {
    def isAbsolute = true
  }
  private final case class ParentIn[B,T,S](parent: Path[B,T,S]) extends Path[B,T,S] {
    def isAbsolute = parent.isAbsolute
  }
  private final case class DirIn[B,T,S](parent: Path[B,T,S], name: DirName) extends Path[B,T,S] {
    def isAbsolute = parent.isAbsolute
  }
  private final case class FileIn[B,T,S](parent: Path[B,T,S], name: FileName) extends Path[B,T,S] {
    def isAbsolute = parent.isAbsolute
  }

  type RelFile[S] = Path[Rel, File, S]
  type AbsFile[S] = Path[Abs, File, S]
  type RelDir[S] = Path[Rel, Dir, S]
  type AbsDir[S] = Path[Abs, Dir, S]

  def currentDir[S]: Path[Rel, Dir, S] = Current

  def rootDir[S]: Path[Abs, Dir, S] = Root

  def file[S](name: String): Path[Rel, File, S] = file1(FileName(name))

  def file1[S](name: FileName): Path[Rel, File, S] = FileIn(Current, name)

  def fileName[B,S](path: Path[B, File, S]): FileName = path match {
    case FileIn(_, name) => name
    case _               => sys.error("impossible!")
  }

  def dir[S](name: String): Path[Rel, Dir, S] = dir1(DirName(name))

  def dir1[S](name: DirName): Path[Rel, Dir, S] = DirIn(Current, name)

  def dirName[B,S](path: Path[B, Dir, S]): Option[DirName] = path match {
    case DirIn(_, name) => Some(name)
    case _              => None
  }

  implicit class PathOps[B,T,S](path: Path[B,T,S]) {
    def relativeTo[SS](dir: Path[B, Dir, SS]): Option[Path[Rel, T, SS]] = {
      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def go[TT](p1: Path[B,TT,S], p2: Path[B,Dir,SS]): Option[Path[Rel,TT,SS]] =
        if (identicalPath(p1, p2)) Some(Current)
        else peel(p1) match {
          case None => (p1, p2) match {
            case (Root, Root)       => Some(Current)
            case (Current, Current) => Some(Current)
            case _                  => None
          }
          case Some((p1p, v)) =>
            go(p1p, p2).map(p => p </> v.fold[Path[Rel,TT,SS]](DirIn(Current, _), FileIn(Current, _)))
          }
      go(canonicalize(path), canonicalize(dir))
    }
  }

  implicit class DirOps[B,S](dir: Path[B, Dir, S]) {
    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def </>[T](rel: Path[Rel, T, S]): Path[B, T, S] =
      (dir, rel) match {
        case (Current,        Current) => Current
        case (Root,           Current) => Root
        case (ParentIn(p1),   Current) => ParentIn(p1 </> Current)
        case (FileIn(p1, f1), Current) => FileIn(p1 </> Current, f1)
        case (DirIn(p1, d1),  Current) => DirIn(p1 </> Current, d1)

        // these don't make sense, but cannot exist anyway
        case (Current,        Root) => Current
        case (Root,           Root) => Root
        case (ParentIn(p1),   Root) => ParentIn(p1 </> Current)
        case (FileIn(p1, f1), Root) => FileIn(p1 </> Current, f1)
        case (DirIn(p1, d1),  Root) => DirIn(p1 </> Current, d1)

        case (p1, ParentIn(p2))   => ParentIn(p1 </> p2)
        case (p1, FileIn(p2, f2)) => FileIn(p1 </> p2, f2)
        case (p1, DirIn(p2, d2))  => DirIn(p1 </> p2, d2)
      }

    // NB: scala doesn't cotton to `<..>`
    def <::>[T](rel: Path[Rel, T, S]): Path[B, T, Unsandboxed] =
      parentDir1(dir) </> unsandbox(rel)
  }

  implicit class FileOps[B,S](file: Path[B, File, S]) {
    // NB: scala doesn't cotton to `<.>`
    def <:>(ext: String): Path[B, File, S] =
      renameFile(file, name => name.changeExtension(_ => ext))
  }

  def refineType[B,T,S](path: Path[B,T,S]): Path[B,Dir,S] \/ Path[B,File,S] = path match {
    case Current      => Current.left
    case Root         => Root.left
    case ParentIn(p)  => ParentIn(unsafeCoerceType(p)).left
    case FileIn(p, f) => FileIn(unsafeCoerceType(p), f).right
    case DirIn(p, d)  => DirIn(unsafeCoerceType(p), d).left
  }

  def maybeDir[B,T,S](path: Path[B,T,S]): Option[Path[B, Dir, S]] =
    refineType(path).swap.toOption

  def maybeFile[B,T,S](path: Path[B,T,S]): Option[Path[B, File, S]] =
    refineType(path).toOption

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def peel[B,T,S](path: Path[B,T,S]): Option[(Path[B, Dir, S], DirName \/ FileName)] = path match {
    case Current         => None
    case Root            => None
    case p @ ParentIn(_) =>
      val (c, p1) = canonicalize1(p)
      if (c) peel(p1) else None
    case DirIn(p, d)     => Some(unsafeCoerceType(p) -> -\/ (d))
    case FileIn(p, f)    => Some(unsafeCoerceType(p) ->  \/-(f))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def depth[B,T,S](path: Path[B,T,S]): Int = path match {
    case Current      => 0
    case Root         => 0
    case ParentIn(p)  => depth(p) - 1
    case FileIn(p, _) => depth(p) + 1
    case DirIn(p, _)  => depth(p) + 1
  }

  def parentDir[B,T,S](path: Path[B,T,S]): Option[Path[B,Dir,S]] =
    peel(path).map(_._1)

  def fileParent[B,S](file: Path[B, File, S]): Path[B, Dir, S] = file match {
    case FileIn(p, _) => unsafeCoerceType(p)
    case _            => sys.error("impossible!")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def unsandbox[B,T,S](path: Path[B,T,S]): Path[B, T, Unsandboxed] = path match {
    case Current      => Current
    case Root         => Root
    case ParentIn(p)  => ParentIn(unsandbox(p))
    case DirIn(p, d)  => DirIn(unsandbox(p), d)
    case FileIn(p, f) => FileIn(unsandbox(p), f)
  }

  /** Synonym for relativeTo, constrained to sandboxed dirs, and with a more evocative name. */
  def sandbox[B,T,S](dir: Path[B, Dir, Sandboxed], path: Path[B,T,S]): Option[Path[Rel,T,Sandboxed]] =
    path relativeTo dir

  def parentDir1[B,T,S](path: Path[B,T,S]): Path[B, Dir, Unsandboxed] =
    ParentIn(unsafeCoerceType(unsandbox(path)))

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def unsafeCoerceType[B,T,TT,S](path: Path[B,T,S]): Path[B,TT,S] = path match {
    case Current      => Current
    case Root         => Root
    case ParentIn(p)  => ParentIn(unsafeCoerceType(p))
    case DirIn(p, d)  => DirIn(unsafeCoerceType(p), d)
    case FileIn(p, f) => FileIn(unsafeCoerceType(p), f)
  }

  def renameFile[B,S](path: Path[B, File, S], f: FileName => FileName): Path[B, File, S] =
    path match {
      case FileIn(p, f0) => FileIn(p, f(f0))
      case p             => p
    }

  def renameDir[B,S](path: Path[B, Dir, S], f: DirName => DirName): Path[B, Dir, S] =
    path match {
      case DirIn(p, d) => DirIn(p, f(d))
      case p           => p
    }

  def canonicalize[B,T,S](path: Path[B,T,S]): Path[B,T,S] =
    canonicalize1(path)._2

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def canonicalize1[B,T,S](path: Path[B,T,S]): (Boolean, Path[B,T,S]) =
    path match {
      case Current                => false -> Current
      case Root                   => false -> Root
      case ParentIn(FileIn(p, f)) => true -> canonicalize1(p)._2
      case ParentIn(DirIn(p, f))  => true -> canonicalize1(p)._2
      case ParentIn(p)            =>
        val (ch, p1) = canonicalize1(p)
        val p2 = ParentIn(p1)
        if (ch) canonicalize1(p2) else ch -> p2  // ???
      case FileIn(p, f)           =>
        val (ch, p1) = canonicalize1(p)
        ch -> FileIn(p1, f)
      case DirIn(p, d)            =>
        val (ch, p1) = canonicalize1(p)
        ch -> DirIn(p1, d)
    }

  def flatten[X](root: => X, currentDir: => X, parentDir: => X, dirName: String => X, fileName: String => X, path: Path[_, _, _]): OneAnd[IList, X] = {
    @tailrec
    def go(xs: OneAnd[IList, X], at: Path[_, _, _]): OneAnd[IList, X] = {
      val tl = xs.head :: xs.tail

      at match {
        case Current      => OneAnd(currentDir, tl)
        case Root         => OneAnd(root, tl)
        case ParentIn(p)  => go(OneAnd(parentDir, tl), p)
        case DirIn(p, d)  => go(OneAnd(dirName(d.value), tl), p)
        case FileIn(p, f) => go(OneAnd(fileName(f.value), tl), p)
      }
    }

    path match {
      case Current      => OneAnd(currentDir, IList.empty)
      case Root         => OneAnd(root, IList.empty)
      case ParentIn(p)  => go(OneAnd(parentDir, IList.empty), p)
      case DirIn(p, d)  => go(OneAnd(dirName(d.value), IList.empty), p)
      case FileIn(p, f) => go(OneAnd(fileName(f.value), IList.empty), p)
    }
  }

  def identicalPath[B,T,S,BB,TT,SS](p1: Path[B,T,S], p2: Path[BB,TT,SS]): Boolean =
    p1.shows ≟ p2.shows

  val posixCodec: PathCodec = PathCodec placeholder '/'

  val windowsCodec: PathCodec = PathCodec placeholder '\\'

  final case class PathCodec(separator: Char, escape: String => String, unescape: String => String) {

    def unsafePrintPath(path: Path[_, _, _]): String = {
      val s = flatten("", ".", "..", escape, escape, path)
                .intercalate(separator.toString)

      maybeDir(path) ? (s ⊹ separator.shows) | s
    }

    def printPath[B, T](path: Path[B, T, Sandboxed]): String =
      unsafePrintPath(path)

    def parsePath[Z](
      rf: RelFile[Unsandboxed] => Z,
      af: AbsFile[Unsandboxed] => Z,
      rd: RelDir[Unsandboxed] => Z,
      ad: AbsDir[Unsandboxed] => Z)(str: String): Z =
    {
      val segs = str.split(separator)
      val isAbs = str.startsWith(separator.toString)
      val isDir = List(separator.toString, s"$separator.", s"$separator..").exists(str.endsWith) || str === "." || str === ".."

      def folder[B,S](base: Path[B,Dir,S], segments: String): Path[B,Dir,S] = segments match {
        case ""    => base
        case "."   => base
        case ".."  => ParentIn(base)
        case seg   => base </> dir(unescape(seg))
      }

      if (str ≟ "")
        rd(Current)
      else if (isAbs && !isDir)
        af(segs.init.foldLeft[AbsDir[Unsandboxed]](rootDir[Unsandboxed])(folder) </> file[Unsandboxed](unescape(segs.last)))
      else if (isAbs && isDir)
        ad(segs.foldLeft[AbsDir[Unsandboxed]](rootDir[Unsandboxed])(folder))
      else if (!isAbs && !isDir)
        rf(segs.init.foldLeft[RelDir[Unsandboxed]](Current)(folder) </> file[Unsandboxed](unescape(segs.last)))
      else
        rd(segs.foldLeft[RelDir[Unsandboxed]](Current)(folder))
    }

    val parseRelFile: String => Option[RelFile[Unsandboxed]] =
      parsePath[Option[RelFile[Unsandboxed]]](Some(_), _ => None, _ => None, _ => None)

    val parseAbsFile: String => Option[AbsFile[Unsandboxed]] =
      parsePath[Option[AbsFile[Unsandboxed]]](_ => None, Some(_), _ => None, _ => None)

    val parseRelDir: String => Option[RelDir[Unsandboxed]] =
      parsePath[Option[RelDir[Unsandboxed]]](_ => None, _ => None, Some(_), _ => None)

    val parseAbsDir: String => Option[AbsDir[Unsandboxed]] =
      parsePath[Option[AbsDir[Unsandboxed]]](_ => None, _ => None, _ => None, Some(_))

    private def asDir[B,S](path: Path[B, File, S]): Path[B, Dir, S] = path match {
      case FileIn(p, FileName(n)) => DirIn(unsafeCoerceType(p), DirName(n))
      case _ => sys.error("impossible!")
    }

    val parseRelAsDir: String => Option[RelDir[Unsandboxed]] =
      parsePath[Option[RelDir[Unsandboxed]]](p => Some(asDir(p)), _ => None, Some(_), _ => None)

    val parseAbsAsDir: String => Option[AbsDir[Unsandboxed]] =
      parsePath[Option[AbsDir[Unsandboxed]]](_ => None, p => Some(asDir(p)), _ => None, Some(_))

  }

  object PathCodec {

    /**
     * The placeholder codec, replaces literal instances of the separator
     * in segments with a placeholder as well as segments equal to either of the
     * relative dir literals, "." and "..".
     */
    def placeholder(sep: Char): PathCodec = {
      val escapeSep = (_: String).replaceAllLiterally(sep.toString, $sep$)
      val unescapeSep = (_: String).replaceAllLiterally($sep$, sep.toString)

      PathCodec(sep, escapeRel compose escapeSep, unescapeSep compose unescapeRel)
    }

    private val escapeRel = (s: String) =>
      if (s ≟ "..") $dotdot$ else if (s ≟ ".") $dot$ else s

    private val unescapeRel = (s: String) =>
      if (s ≟ $dotdot$) ".." else if (s ≟ $dot$) "." else s

    private val $sep$ = "$sep$"
    private val $dot$ = "$dot$"
    private val $dotdot$ = "$dotdot$"
  }

  implicit def pathShow[B,T,S]: Show[Path[B,T,S]] = new Show[Path[B,T,S]] {
    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    override def show(v: Path[B,T,S]): Cord = v match {
      case Current                => "currentDir"
      case Root                   => "rootDir"
      case ParentIn(p)            => "parentDir(" ⊹ p.shows ⊹ ")"
      case FileIn(p, FileName(f)) => p.shows ⊹ " </> file(" ⊹ f.shows ⊹ ")"
      case DirIn(p, DirName(d))   => p.shows ⊹ " </> dir(" ⊹ d.shows ⊹ ")"
    }
  }

  implicit def pathOrder[B,T,S]: Order[Path[B,T,S]] =
    Order.orderBy(p =>
      flatten[(Option[Int], Option[String \/ String])](
        root       =      (some(0),          none),
        parentDir  =      (some(1),          none),
        currentDir =      (some(2),          none),
        dirName    = s => (none   , some( s.left)),
        fileName   = s => (none   , some(s.right)),
        path       = p))
}
