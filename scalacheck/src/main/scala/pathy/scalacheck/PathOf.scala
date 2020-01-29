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
package scalacheck

import slamdata.Predef._
import Path._
import PathNameOf._

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scalaz.Show

/** Represents a `Path[B,T,S]` indexed by another type `A` which is used to
  * generate path segments.
  *
  * i.e. For some type `A` such that Arbitrary[A] and Show[A],
  * Arbitrary[PathOf[B,T,S,A]] will generate an arbitrary Path[B,T,S] where
  * every path segment is formed by the string representation of an arbitrary `A`.
  */
final case class PathOf[B,T,S,A](path: Path[B,T,S]) extends AnyVal

object PathOf {

  implicit def absFileOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Abs,File,Sandboxed,A]] =
    Arbitrary(genAbsFile[A] map (PathOf(_)))

  implicit def relFileOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Rel,File,Sandboxed,A]] =
    Arbitrary(genRelFile[A] map (PathOf(_)))

  implicit def absDirOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Abs,Dir,Sandboxed,A]] =
    Arbitrary(genAbsDir[A] map (PathOf(_)))

  implicit def relDirOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Rel,Dir,Sandboxed,A]] =
    Arbitrary(genRelDir[A] map (PathOf(_)))

  ////

  private def genAbsFile[A: Arbitrary: Show]: Gen[AbsFile[Sandboxed]] =
    genRelFile[A] map (rootDir </> _)

  private def genRelFile[A: Arbitrary: Show]: Gen[RelFile[Sandboxed]] =
    sizeDistributed(genRelDir[A], genFileName[A])((d, s) => d </> file1(s))

  private def genAbsDir[A: Arbitrary: Show]: Gen[AbsDir[Sandboxed]] =
    genRelDir[A] map (rootDir </> _)

  private def genRelDir[A: Arbitrary: Show]: Gen[RelDir[Sandboxed]] =
    sizeDistributedListOfNonEmpty(genDirName[A])
      .map(_.foldLeft(currentDir[Sandboxed])((d, s) => d </> dir1(s)))
}
