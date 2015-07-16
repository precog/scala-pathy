package slamdata.pathy

import org.specs2.mutable._

class PathSpecs extends Specification {
  import Path._

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

  "parseAbsFile - /image.png" in {
    parseAbsFile("/image.png") must beSome(rootDir </> file("image.png"))
  }

  "parseAbsFile - /foo/image.png" in {
    parseAbsFile("/foo/image.png") must beSome(rootDir </> dir("foo") </> file("image.png"))
  }

  "parseRelDir - empty string" in {
    parseRelDir("") must beSome(currentDir[Sandboxed])
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

  "parseRelDir - ./foo/bar/" in {
    parseRelDir("./foo/bar/") must beSome(dir("foo") </> dir("bar"))
  }

  "parseAbsDir - /" in {
    parseAbsDir("/") must beSome(rootDir[Sandboxed])
  }

  "parseAbsDir - /foo/" in {
    parseAbsDir("/foo/") must beSome(rootDir </> dir("foo"))
  }

  "parseAbsDir - /foo/bar/" in {
    parseAbsDir("/foo/bar/") must beSome(rootDir </> dir("foo") </> dir("bar"))
  }
}
