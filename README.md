[![Build Status](https://travis-ci.org/slamdata/scala-pathy.svg?branch=master)](https://travis-ci.org/slamdata/scala-pathy)

# pathy

A type-safe abstraction for platform-independent file system paths.

Ported from [purescript-pathy](slamengine/purescript-pathy).

## Example

```scala
val fullPath = rootDir </> dir("baz") </> file("foo.png")
```
See the [examples file](tests/src/test/scala/examples.scala) for more.

## Getting Started

### Installation

pathy is cross-built against Scala 2.10.x and 2.11.x.

If you're using SBT, add the following to your build file

```scala
libraryDependencies += "com.slamdata" %% "pathy" % "0.0.1-SNAPSHOT"
```

### Usage

The following imports will bring all types and operations into scope

```scala
import pathy._, Path._
````
