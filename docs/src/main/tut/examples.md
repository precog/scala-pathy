---
layout: source
title:  "Examples"
section: "examples"
scaladoc: "http://slamdata.github.io/scala-pathy/api/#examples.ExamplesExamples"
source: "http://slamdata.github.io/scala-pathy/blob/master/example/src/main/scala/Examples.scala"
pageSource: "https://raw.githubusercontent.com/slamdata/scala-pathy/master/docs/src/main/tut/examples.md"
---
# Pathy Examples

## Constructing paths

Typically, you want to import both `Path` and all of the helpers in its companion:

```tut:silent
import pathy.Path, Path._
```

To create paths, use either `rootDir` or `currentDir`, and then add segments with `</>`, `dir`, and `file`.

```tut:silent

rootDir </> file("afile.txt")

currentDir </> dir("somedir")
```

Note that the resulting type tracks the kind of path:

```tut:silent
val p1: Path[Abs, File, Sandboxed] = rootDir </> file("afile.txt")
val p2: Path[Rel, Dir, Sandboxed] = currentDir </> dir("somedir")
```

The special operator `<::>` builds paths that refer to parent directories. Note that the resulting path is always `Unsandboxed`, meaning you can't be sure that it refers to a file inside the current directory:

```tut:silent
val p3: Path[Rel, Dir, Unsandboxed] = currentDir <::> dir("somedir")
```

If the types don't match, you get a _compile time_ error:

```tut:fail
val impossible: Path[Rel, Dir, Sandboxed] = rootDir </> file("afile.txt")
```

A `Show` instance is provided, which renders paths as they would be entered in source code:

```tut:silent
import scalaz._, Scalaz._
```

```tut
p1.shows
```

Finally, type aliases are defined for the four interesting combinations, and you can often ignore sandboxing:

```tut:
val p1a: AbsFile[_] = p1
```

## Parsing paths

A _codec_ provides simple parsing and printing of paths. A couple of simple codecs are provided:

```tut:silent
val p4: Option[Path[Abs, Dir, Unsandboxed]] = posixCodec.parseAbsDir("/")
val p5: Option[Path[Rel, File, Unsandboxed]] = windowsCodec.parseRelFile(".\\My Documents\\bob.bmp")
```

If you try to parse a path of the wrong type, pathy lets you know:

```tut
posixCodec.parseRelFile("/actuallyADir/")
```

To print a path, it first needs to be "sandboxed", that is, made relative to some reference path:

```tut
for {
  p <- posixCodec.parseRelFile("./foo/bar/baz.txt")
  p <- sandbox(currentDir </> dir("foo"), p)
} yield posixCodec.printPath(p)
```


## Working with paths

_TODO: examples for the mundane stuff_

If you know you're dealing with a File path, some additional functions are available:

```tut:silent
val p6 = rootDir </> file("file.txt")
```

```tut
renameFile(p6, name => FileName(name.value.toUpperCase)).shows
(p1 <:> "jpg").shows
```

A file path _always_ has both a parent direcory and a file name at the end:

```tut
fileParent(p6)
fileName(p6)
```

On the other hand, a directory path may or may not actually contain any directory names:

```tut
dirName(currentDir)
```
