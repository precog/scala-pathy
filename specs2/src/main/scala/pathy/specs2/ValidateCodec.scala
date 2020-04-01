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
import pathy.Path._
import pathy.scalacheck.PathyArbitrary._

import org.specs2.mutable.SpecLike
import org.specs2.specification.core.Fragment
import org.specs2.ScalaCheck

trait ValidateCodec extends SpecLike with ScalaCheck {

  /** It's protected to emphasize it doesn't make any sense to be
   *  calling this method except from the instance which inherits it.
   *  Mutable specifications work by mutating an instance variable.
   */
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  protected def validateIsLossless(codec: PathCodec): Fragment = {
    "print and parse again should produce same Path" >> {
      "absolute file" >> prop { path: AbsFile[Sandboxed] =>
        codec.parseAbsFile(codec.printPath(path)) must_== Some(path)
      }
      "relative file" >> prop { path: RelFile[Sandboxed] =>
        codec.parseRelFile(codec.printPath(path)) must_== Some(path)
      }
      "absolute dir" >> prop { path: AbsDir[Sandboxed] =>
        codec.parseAbsDir(codec.printPath(path)) must_== Some(path)
      }
      "relative dir" >> prop { path: RelDir[Sandboxed] =>
        codec.parseRelDir(codec.printPath(path)) must_== Some(path)
      }
    }
  }
}
