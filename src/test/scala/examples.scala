import slamdata.pathy._, Path._

object Examples extends App {

  println(FileName("foo.png").extension)
  println(FileName("foo.png").dropExtension)
  println(FileName("foo.png").changeExtension(_ => "svg"))
  println(FileName("foo.png").changeExtension(x => x.toUpperCase))

  val abs1: Path[Abs, File, Sandboxed] = rootDir </> dir("foo") </> file("bar")
  println(printPath(abs1))

  val rel1: Path[Rel, File, Unsandboxed] = currentDir </> dir("foo") <::> file("bar")
  val rel2: Option[Path[Rel, File, Sandboxed]] = rel1 relativeTo (currentDir </> dir("foo"))
  println(printPath(rel2.get))
}
