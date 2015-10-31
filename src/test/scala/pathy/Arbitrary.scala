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

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import pathy.Path._

import scalaz.syntax.functor._
import scalaz.std.list._

package object arbitrary {

  implicit val arbitraryAbsFile: Arbitrary[AbsFile[Sandboxed]] =
    Arbitrary(Gen.resize(10, genAbsFile))

  implicit val arbitraryRelFile: Arbitrary[RelFile[Sandboxed]] =
    Arbitrary(Gen.resize(10, genRelFile))

  implicit val arbitraryAbsDir: Arbitrary[AbsDir[Sandboxed]] =
    Arbitrary(Gen.resize(10, genAbsDir))

  implicit val arbitraryRelDir: Arbitrary[RelDir[Sandboxed]] =
    Arbitrary(Gen.resize(10, genRelDir))

  def genAbsFile: Gen[AbsFile[Sandboxed]] =
    genRelFile map (rootDir </> _)

  def genRelFile: Gen[RelFile[Sandboxed]] =
    for {
      d <- genRelDir
      s <- genSegment
    } yield d </> file(s)

  def genAbsDir: Gen[AbsDir[Sandboxed]] =
    genRelDir map (rootDir </> _)

  def genRelDir: Gen[RelDir[Sandboxed]] =
    Gen.frequency(
      (  1, Gen.const(currentDir[Sandboxed])),
      (100, Gen.nonEmptyListOf(genSegment)
        .map(_.foldLeft(currentDir[Sandboxed])((d, s) => d </> dir(s)))))

  // TODO: Are these special characters MongoDB-specific?
  def genSegment: Gen[String] =
    Gen.nonEmptyListOf(Gen.frequency(
      (100, Arbitrary.arbitrary[Char]) ::
        "./".toList.map(Gen.const).strengthL(10): _*))
      .map(_.mkString)
}
