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


  /*
  TODO: port the rest

    test' "sandbox - sandbox absolute dir to one level higher"
          (fromJust $ sandbox (rootDir </> dir "foo") (rootDir </> dir "foo" </> dir "bar")) "./bar/"

    test "depth - negative" (depth (parentDir' $ parentDir' $ parentDir' $ currentDir)) (-3)

    test "parseRelFile - image.png" (parseRelFile "image.png") (Just $ file "image.png")

    test "parseRelFile - ./image.png" (parseRelFile "./image.png") (Just $ file "image.png")

    test "parseRelFile - foo/image.png" (parseRelFile "foo/image.png") (Just $ dir "foo" </> file "image.png")

    test "parseRelFile - ../foo/image.png" (parseRelFile "../foo/image.png") (Just $ currentDir <..> dir "foo" </> file "image.png")

    test "parseAbsFile - /image.png" (parseAbsFile "/image.png") (Just $ rootDir </> file "image.png")

    test "parseAbsFile - /foo/image.png" (parseAbsFile "/foo/image.png") (Just $ rootDir </> dir "foo" </> file "image.png")

    test "parseRelDir - empty string" (parseRelDir "") (Just $ currentDir)

    test "parseRelDir - ./../" (parseRelDir "./../") (Just $ currentDir <..> currentDir)

    test "parseRelDir - foo/" (parseRelDir "foo/") (Just $ dir "foo")

    test "parseRelDir - foo/bar" (parseRelDir "foo/bar/") (Just $ dir "foo" </> dir "bar")

    test "parseRelDir - ./foo/bar" (parseRelDir "./foo/bar/") (Just $ dir "foo" </> dir "bar")

    test "parseAbsDir - /" (parseAbsDir "/") (Just $ rootDir)

    test "parseAbsDir - /foo/" (parseAbsDir "/foo/") (Just $ rootDir </> dir "foo")

    test "parseAbsDir - /foo/bar" (parseAbsDir "/foo/bar/") (Just $ rootDir </> dir "foo" </> dir "bar")
  */
}
