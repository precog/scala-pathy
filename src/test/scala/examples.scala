import slamdata.pathy._, Path._
import scalaz._, Scalaz._

object Examples extends App {
  import posixCodec._

  println(FileName("foo.png").extension)
  println(FileName("foo.png").dropExtension)
  println(FileName("foo.png").changeExtension(_ => "svg"))
  println(FileName("foo.png").changeExtension(x => x.toUpperCase))

  val abs1: Path[Abs, File, Sandboxed] = rootDir </> dir("foo") </> file("bar")
  println(printPath(abs1))

  println(abs1.show)

  val rel1: Path[Rel, File, Unsandboxed] = currentDir </> dir("foo") </> file("bar")
  val rel2: Option[Path[Rel, File, Sandboxed]] = rel1 relativeTo (currentDir </> dir("foo"))
  println(printPath(rel2.get))
}
