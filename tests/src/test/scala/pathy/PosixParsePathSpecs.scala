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

import org.specs2.mutable.Specification
import pathy.Path._

class PosixParsePathSpecs extends Specification {
  import posixCodec._

  "parseRelFile" >> {
    "should successfully parse" >> {

      "simple file name with extension " in {
        parseRelFile("image.png") must beSome(file("image.png"))
      }

      "preceded with current directory" in {
        parseRelFile("./image.png") must beSome(file("image.png"))
      }

      "path with two segments" in {
        parseRelFile("foo/image.png") must beSome(dir("foo") </> file("image.png"))
      }

      "preceded by a parent directory reference" in {
        parseRelFile("../foo/image.png") must beSome(currentDir <::> dir("foo") </> file("image.png"))
      }
    }

    "should fail to parse" >> {

      "with a leading /" in {
        parseRelFile("/foo/image.png") must beNone
      }

      "with a trailing /" in {
        parseRelFile("foo/") must beNone
      }

      "." in {
        parseRelFile(".") must beNone
      }

      "foo/.." in {
        parseRelFile("foo/..") must beNone
      }
    }
  }

  "parseAbsFile" >> {
    "should successfully parse" >> {
      "a simple filename with extensions as long as there is a leading /" in {
        parseAbsFile("/image.png") must beSome(rootDir </> file("image.png"))
      }

      "a path with two segments" in {
        parseAbsFile("/foo/image.png") must beSome(rootDir </> dir("foo") </> file("image.png"))
      }
    }

    "fail to parse" >> {

      "with a trailing /" in {
        parseAbsFile("/foo/") must beNone
      }

      "with no leading /" in {
        parseAbsFile("foo/image.png") must beNone
      }

      "/." in {
        parseAbsFile("/.") must beNone
      }

      "/foo/.." in {
        parseAbsFile("/foo/..") must beNone
      }
    }
  }

  "parseRelDir" >> {

    "should successfully parse" >> {

      "empty string" in {
        parseRelDir("") must beSome(currentDir[Unsandboxed])
      }

      "./../" in {
        parseRelDir("./../") must beSome(currentDir <::> currentDir)
      }

      "segment with trailing /" in {
        parseRelDir("foo/") must beSome(dir("foo"))
      }

      "segment with trailing ." in {
        parseRelDir("foo/.") must beSome(dir("foo") </> currentDir)
      }

      "two segments with trailing /" in {
        parseRelDir("foo/bar/") must beSome(dir("foo") </> dir("bar"))
      }

      "two segments starting with a reference to current directory" in {
        parseRelDir("./foo/bar/") must beSome(dir("foo") </> dir("bar"))
      }
    }

    "fail to parse" >> {

      "leading with a /" in {
        parseRelDir("/foo/") must beNone
      }

      "simple name" in {
        parseRelDir("foo") must beNone
      }
    }

  }

  "parseRelAsDir" >> {
    "./foo/bar" in {
      parseRelAsDir("./foo/bar") must beSome(dir("foo") </> dir("bar"))
    }

    "./foo/bar/" in {
      parseRelAsDir("./foo/bar/") must beSome(dir("foo") </> dir("bar"))
    }
  }

  "parseAbsDir" >> {
    "should successfully parse" >> {

      "/" in {
        parseAbsDir("/") must beSome(rootDir[Unsandboxed])
      }

      "/foo/" in {
        parseAbsDir("/foo/") must beSome(rootDir </> dir("foo"))
      }

      "/foo/bar/" in {
        parseAbsDir("/foo/bar/") must beSome(rootDir </> dir("foo") </> dir("bar"))
      }
    }

    "should fail to parse" >> {

      "/foo" in {
        parseAbsDir("/foo") must beNone
      }

      "foo" in {
        parseAbsDir("foo") must beNone
      }
    }
  }

  "parseAbsAsDir" >> {
    "/foo/bar/" in {
      parseAbsAsDir("/foo/bar/") must beSome(rootDir </> dir("foo") </> dir("bar"))
    }

    "/foo/bar" in {
      parseAbsAsDir("/foo/bar") must beSome(rootDir </> dir("foo") </> dir("bar"))
    }
  }
}
