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

import pathy.scalacheck._

import org.specs2.mutable._
import org.specs2.ScalaCheck

object ValidateCodec extends SpecificationLike with ScalaCheck {
  import Path._
  import PathyArbitrary._

  def validateIsLossless(codec: PathCodec) = {
    "print and parse again should produce same Path" >> {
      "absolute file" ! prop { path: AbsFile[Sandboxed] =>
        codec.parseAbsFile(codec.printPath(path)) must_== Some(path)
      }
      "relative file" ! prop{ path: RelFile[Sandboxed] =>
        codec.parseRelFile(codec.printPath(path)) must_== Some(path)
      }
      "absolute dir" ! prop { path: AbsDir[Sandboxed] =>
        codec.parseAbsDir(codec.printPath(path)) must_== Some(path)
      }
      "relative dir" ! prop { path: RelDir[Sandboxed] =>
        codec.parseRelDir(codec.printPath(path)) must_== Some(path)
      }
    }
  }
}
