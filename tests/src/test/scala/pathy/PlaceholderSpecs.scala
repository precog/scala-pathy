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

import org.specs2.mutable.Spec
import pathy.Path._

class PlaceholderSpecs extends Spec with ValidateCodec {
  import posixCodec._

  "placeholder codec" >> {
    "printPath" >> {
      "replaces separator in segments with placeholder" in {
        unsafePrintPath(dir("foo/bar") </> dir("baz") </> file("qu/ux.txt")) must_== "./foo$sep$bar/baz/qu$sep$ux.txt"
      }

      "replaces single dot dir name with placeholder" in {
        unsafePrintPath(dir(".") </> file("config")) must_== "./$dot$/config"
      }

      "replaces double dot dir name with placeholder" in {
        unsafePrintPath(dir("foo") </> dir("..") </> file("config")) must_== "./foo/$dotdot$/config"
      }
    }

    "parsePath" >> {
      /* Weirdly, in these examples must_=== compiles under scala 2.11 but not 2.10. */
      "reads separator ph in segments" in {
        parseRelDir("foo/$sep$/bar/") must_== Some(dir("foo") </> dir("/") </> dir("bar"))
      }

      "reads single dot ph in segments" in {
        parseRelFile("foo/$dot$/bar") must_== Some(dir("foo") </> dir(".") </> file("bar"))
      }

      "reads double dot separator in segments" in {
        parseRelFile("foo/bar/$dotdot$") must_== Some(dir("foo") </> dir("bar") </> file(".."))
      }
    }

    "posixCodec is lossless" >> validateIsLossless(posixCodec)

    "windowsCodec is lossless" >> validateIsLossless(windowsCodec)
  }
}
