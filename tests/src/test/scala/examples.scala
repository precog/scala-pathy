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

import pathy._, Path._
import scalaz._, Scalaz._

object Examples extends App {
  import posixCodec._

  println(FileName("foo.png").extension)
  println(FileName("foo.png").dropExtension)
  println(FileName("foo.png").changeExtension(_ => "svg"))
  println(FileName("foo.png").changeExtension(x => x.toUpperCase))

  val abs1: Path[Abs, File, Sandboxed] = rootDir[Sandboxed] </> dir("foo") </> file("bar")
  println(printPath(abs1))

  println(abs1.show)

  val rel1: Path[Rel, File, Unsandboxed] = currentDir[Unsandboxed] </> dir("foo") </> file("bar")
  val rel2: Option[Path[Rel, File, Unsandboxed]] = rel1 relativeTo (currentDir </> dir("foo"))
  println(unsafePrintPath(rel2.get))
}
