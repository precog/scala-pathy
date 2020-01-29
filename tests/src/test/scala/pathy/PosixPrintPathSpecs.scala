/*
 * Copyright 2014–2020 SlamData Inc.
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

import org.specs2.mutable.Specification
import pathy.Path._

class PosixPrintPathSpecs extends Specification {
  import posixCodec._

  "two directories" in {
    unsafePrintPath(dir("foo") </> file("bar")) must_=== "./foo/bar"
  }

  "file with two parents" in {
    unsafePrintPath(dir("foo") </> dir("bar") </> file("image.png")) must_=== "./foo/bar/image.png"
  }

  "file without extension" in {
    unsafePrintPath(file("image") <:> "png") must_=== "./image.png"
  }

  "file with extension" in {
    unsafePrintPath(file("image.jpg") <:> "png") must_=== "./image.png"
  }

  "printPath - ./../" in {
    unsafePrintPath(parentDir1(currentDir)) must_=== "./../"
  }
}
