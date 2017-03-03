/*
 * Copyright 2014–2017 SlamData Inc.
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
import org.specs2.mutable.Specification
import pathy.Path._
import scalaz._, Scalaz._

abstract class PlaceholderSpecs(pathCodec: PathCodec) extends Specification {

  import ValidateCodec.validateIsLossless

  val c: Char = pathCodec.separator

  "placeholder codec" in {
    "printPath" >> {
      "replaces separator in segments with placeholder" in {
        PathCodec.placeholder(c)
          .unsafePrintPath(dir(s"foo${c}bar") </> dir("baz") </> file(s"qu${c}ux.txt")) must_==
            s".${c}foo$$sep$$bar${c}baz${c}qu$$sep$$ux.txt"
      }

      "replaces single dot dir name with placeholder" in {
        PathCodec.placeholder(c)
          .unsafePrintPath(dir(".") </> file("config")) must_== s".${c}$$dot$$${c}config"
      }

      "replaces double dot dir name with placeholder" in {
        PathCodec.placeholder(c)
          .unsafePrintPath(dir("foo") </> dir("..") </> file("config")) must_== s".${c}foo${c}$$dotdot$$${c}config"
      }
    }

    "parsePath" >> {
      "reads separator ph in segments" in {
        PathCodec.placeholder(c)
          .parseRelDir(s"foo${c}$$sep$$${c}bar${c}") must beSome(dir("foo") </> dir(c.shows) </> dir("bar"))
      }

      "reads single dot ph in segments" in {
        PathCodec.placeholder(c)
          .parseRelFile(s"foo${c}$$dot$$${c}bar") must beSome(dir("foo") </> dir(".") </> file("bar"))
      }

      "reads double dot separator in segments" in {
        PathCodec.placeholder(c)
          .parseRelFile(s"foo${c}bar${c}$$dotdot$$") must beSome(dir("foo") </> dir("bar") </> file(".."))
      }
    }

    "PathCodec is lossless" in validateIsLossless(pathCodec)
  }
}

class PosixPlaceholderSpecs extends PlaceholderSpecs(posixCodec)

class WindowsPlaceholderSpecs extends PlaceholderSpecs(windowsCodec)
