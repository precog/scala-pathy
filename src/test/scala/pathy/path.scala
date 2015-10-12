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

import org.specs2.mutable._
import scalaz.syntax.foldable._

class PathSpecs extends Specification {
  import Path._
  import posixCodec._

  "two directories" in {
    unsafePrintPath(dir("foo") </> file("bar")) must_== "./foo/bar"
  }

  "file with two parents" in {
    unsafePrintPath(dir("foo") </> dir("bar") </> file("image.png")) must_== "./foo/bar/image.png"
  }

  "file without extension" in {
    unsafePrintPath(file("image") <:> "png") must_== "./image.png"
  }

  "file with extension" in {
    unsafePrintPath(file("image.jpg") <:> "png") must_== "./image.png"
  }

  "printPath - ./../" in {
    unsafePrintPath(parentDir1(currentDir)) must_== "./../"
  }

  "</> - ./../foo/" in {
    unsafePrintPath(parentDir1(currentDir) </> unsandbox(dir("foo"))) must_== "./../foo/"
  }

  "parentDir1 - ./../foo/../" in {
    unsafePrintPath((parentDir1(currentDir) </> unsandbox(dir("foo"))) </> parentDir1(currentDir)) must_== "./../foo/../"
  }

  "<::> - ./../" in {
    unsafePrintPath(currentDir <::> currentDir) must_== "./../"
  }

  "<::> - ./../foo/" in {
    unsafePrintPath(currentDir <::> dir("foo")) must_== "./../foo/"
  }

  "<::> - ./../foo/../" in {
    unsafePrintPath((currentDir <::> dir("foo"))  <::> currentDir) must_== "./../foo/../"
  }

  "canonicalize - 1 down, 1 up" in {
    unsafePrintPath(canonicalize(parentDir1(dir("foo")))) must_== "./"
  }

  "canonicalize - 2 down, 2 up" in {
    unsafePrintPath(canonicalize(parentDir1(parentDir1(dir("foo") </> dir("bar"))))) must_== "./"
  }

  "renameFile - single level deep" in {
    unsafePrintPath(renameFile(file("image.png"), _.dropExtension)) must_== "./image"
  }

  "sandbox - sandbox absolute dir to one level higher" in {
    sandbox(rootDir </> dir("foo"), rootDir </> dir("foo") </> dir("bar")) must beSome.which {
      unsafePrintPath(_) must_== "./bar/"
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

  "parseRelFile - image.png" in {
    parseRelFile("image.png") must beSome(file("image.png"))
  }

  "parseRelFile - ./image.png" in {
    parseRelFile("./image.png") must beSome(file("image.png"))
  }

  "parseRelFile - foo/image.png" in {
    parseRelFile("foo/image.png") must beSome(dir("foo") </> file("image.png"))
  }

  "parseRelFile - ../foo/image.png" in {
    parseRelFile("../foo/image.png") must beSome(currentDir <::> dir("foo") </> file("image.png"))
  }

  "parseRelFile - /foo/image.png" in {
    parseRelFile("/foo/image.png") must beNone
  }

  "parseRelFile - foo/" in {
    parseRelFile("foo/") must beNone
  }

  "parseAbsFile - /image.png" in {
    parseAbsFile("/image.png") must beSome(rootDir </> file("image.png"))
  }

  "parseAbsFile - /foo/image.png" in {
    parseAbsFile("/foo/image.png") must beSome(rootDir </> dir("foo") </> file("image.png"))
  }

  "parseAbsFile - /foo/" in {
    parseAbsFile("/foo/") must beNone
  }

  "parseAbsFile - foo/image.png" in {
    parseAbsFile("foo/image.png") must beNone
  }

  "parseRelDir - empty string" in {
    parseRelDir("") must beSome(currentDir[Unsandboxed])
  }

  "parseRelDir - ./../" in {
    parseRelDir("./../") must beSome(currentDir <::> currentDir)
  }

  "parseRelDir - foo/" in {
    parseRelDir("foo/") must beSome(dir("foo"))
  }

  "parseRelDir - foo/bar/" in {
    parseRelDir("foo/bar/") must beSome(dir("foo") </> dir("bar"))
  }

  "parseRelDir - /foo/" in {
    parseRelDir("/foo/") must beNone
  }

  "parseRelDir - foo" in {
    parseRelDir("foo") must beNone
  }

  "parseRelDir - ./foo/bar/" in {
    parseRelDir("./foo/bar/") must beSome(dir("foo") </> dir("bar"))
  }

  "parseRelAsDir - ./foo/bar" in {
    parseRelAsDir("./foo/bar") must beSome(dir("foo") </> dir("bar"))
  }

  "parseRelAsDir - ./foo/bar/" in {
    parseRelAsDir("./foo/bar/") must beSome(dir("foo") </> dir("bar"))
  }

  "parseAbsDir - /" in {
    parseAbsDir("/") must beSome(rootDir[Unsandboxed])
  }

  "parseAbsDir - /foo/" in {
    parseAbsDir("/foo/") must beSome(rootDir </> dir("foo"))
  }

  "parseAbsDir - /foo/bar/" in {
    parseAbsDir("/foo/bar/") must beSome(rootDir </> dir("foo") </> dir("bar"))
  }

  "parseAbsDir - /foo" in {
    parseAbsDir("/foo") must beNone
  }

  "parseAbsDir - foo" in {
    parseAbsDir("foo") must beNone
  }

  "parseAbsAsDir - /foo/bar/" in {
    parseAbsAsDir("/foo/bar/") must beSome(rootDir </> dir("foo") </> dir("bar"))
  }

  "parseAbsAsDir - /foo/bar" in {
    parseAbsAsDir("/foo/bar") must beSome(rootDir </> dir("foo") </> dir("bar"))
  }

  "placeholder codec" in {
    "printPath - replaces separator in segments with placeholder" in {
      unsafePrintPath(dir("foo/bar") </> dir("baz") </> file("qu/ux.txt")) must_== "./foo$sep$bar/baz/qu$sep$ux.txt"
    }

    "printPath - replaces single dot dir name with placeholder" in {
      unsafePrintPath(dir(".") </> file("config")) must_== "./$dot$/config"
    }

    "printPath - replaces double dot dir name with placeholder" in {
      unsafePrintPath(dir("foo") </> dir("..") </> file("config")) must_== "./foo/$dotdot$/config"
    }

    "parsePath - reads separator ph in segments" in {
      parseRelDir("foo/$sep$/bar/") must beSome(dir("foo") </> dir("/") </> dir("bar"))
    }

    "parsePath - reads single dot ph in segments" in {
      parseRelFile("foo/$dot$/bar") must beSome(dir("foo") </> dir(".") </> file("bar"))
    }

    "parsePath - reads double dot separator in segments" in {
      parseRelFile("foo/bar/$dotdot$") must beSome(dir("foo") </> dir("bar") </> file(".."))
    }
  }
}
