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
package scalacheck

import Path._

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scalaz.Show
import scalaz.syntax.show._

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
    Arbitrary(Gen.resize(10, genAbsFile[A] map (PathOf(_))))

  implicit def relFileOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Rel,File,Sandboxed,A]] =
    Arbitrary(Gen.resize(10, genRelFile[A] map (PathOf(_))))

  implicit def absDirOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Abs,Dir,Sandboxed,A]] =
    Arbitrary(Gen.resize(10, genAbsDir[A] map (PathOf(_))))

  implicit def relDirOfArbitrary[A: Arbitrary: Show]: Arbitrary[PathOf[Rel,Dir,Sandboxed,A]] =
    Arbitrary(Gen.resize(10, genRelDir[A] map (PathOf(_))))

  ////

  private def genAbsFile[A: Arbitrary: Show]: Gen[AbsFile[Sandboxed]] =
    genRelFile[A] map (rootDir </> _)

  private def genRelFile[A: Arbitrary: Show]: Gen[RelFile[Sandboxed]] =
    for {
      d <- genRelDir[A]
      s <- genSegment[A]
    } yield d </> file(s)

  private def genAbsDir[A: Arbitrary: Show]: Gen[AbsDir[Sandboxed]] =
    genRelDir[A] map (rootDir </> _)

  private def genRelDir[A: Arbitrary: Show]: Gen[RelDir[Sandboxed]] =
    Gen.frequency(
      (  1, Gen.const(currentDir[Sandboxed])),
      (100, Gen.nonEmptyListOf(genSegment[A])
        .map(_.foldLeft(currentDir[Sandboxed])((d, s) => d </> dir(s)))))

  private def genSegment[A: Arbitrary: Show]: Gen[String] =
    Arbitrary.arbitrary[A] map (_.shows)
}
