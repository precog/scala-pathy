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

import org.specs2.mutable.Specification
import scalaz.syntax.foldable._

class PathSpecs extends Specification {
  import Path._

  "</> - ./../foo/" in {
    posixCodec.unsafePrintPath(parentDir1(currentDir) </> unsandbox(dir("foo"))) must_== "./../foo/"
  }

  "parentDir1 - ./../foo/../" in {
    posixCodec.unsafePrintPath((parentDir1(currentDir) </> unsandbox(dir("foo"))) </> parentDir1(currentDir)) must_== "./../foo/../"
  }

  "<::>" >> {
    "./../" in {
      posixCodec.unsafePrintPath(currentDir <::> currentDir) must_== "./../"
    }

    "./../foo/" in {
      posixCodec.unsafePrintPath(currentDir <::> dir("foo")) must_== "./../foo/"
    }

    "./../foo/../" in {
      posixCodec.unsafePrintPath((currentDir <::> dir("foo")) <::> currentDir) must_== "./../foo/../"
    }
  }

  "canonicalize" >> {
    "1 down, 1 up" in {
      canonicalize(parentDir1(dir("foo"))) must_== currentDir
    }

    "2 down, 2 up" in {
      canonicalize(parentDir1(parentDir1(dir("foo") </> dir("bar")))) must_== currentDir
    }
  }

  "renameFile - single level deep" in {
    renameFile(file("image.png"), _.dropExtension) must_== file("image")
  }

  "sandbox - sandbox absolute dir to one level higher" in {
    sandbox(rootDir </> dir("foo"), rootDir </> dir("foo") </> dir("bar")) must beSome.which {
      _ must_== dir("bar")
    }
  }

  "depth - negative" in {
    depth(parentDir1(parentDir1(parentDir1(currentDir)))) must_== -3
  }

  "flatten - returns NEL of result of folding each layer of path" in {
    flatten(
      "r", "c", "p", identity, identity,
      currentDir </> dir("foo") </> dir("bar") </> file("flat.md")
    ).toList must_== List("c", "foo", "bar", "flat.md")
  }
}
