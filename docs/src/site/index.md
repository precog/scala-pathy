---
layout: default
title: "Pathy"
section: "home"
---
# Pathy

A type-safe abstraction for platform-independent file system paths.

## Getting Started

### Installation

pathy is cross-built against Scala 2.10.x and 2.11.x.

If you're using SBT, add the following to your build file

```scala
libraryDependencies += "com.slamdata" %% "pathy" % "0.0.4-SNAPSHOT"
```

### Usage

The following imports will bring all types and operations into scope

```scala
import pathy._, Path._
````

### Documentation

For more details, see [these examples](tut/examples.html).

Or refer to the [scaladoc](api/index.html).
