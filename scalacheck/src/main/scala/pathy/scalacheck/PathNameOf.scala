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

package pathy.scalacheck

import slamdata.Predef._
import pathy.Path.{DirName, FileName}

import org.scalacheck.{Gen, Arbitrary}
import scalaz.Show
import scalaz.syntax.show._

/** Represents a `FileName` indexed by another type `A` which is used to
  * generate the segment.
  *
  * i.e. For some type `A` such that `Arbitrary[A]` and `Show[A]`,
  * `Arbitrary[FileNameOf[A]]` will generate an arbitrary `FileName` where
  * the path segment is formed by the string representation of an arbitrary `A`.
  */
final case class FileNameOf[A](filename: FileName)

/** Represents a `DirName` indexed by another type `A` which is used to
  * generate the segment.
  *
  * i.e. For some type `A` such that `Arbitrary[A]` and `Show[A]`,
  * `Arbitrary[DirNameOf[A]]` will generate an arbitrary `DirName` where
  * the path segment is formed by the string representation of an arbitrary `A`.
  */
final case class DirNameOf[A](dirname: DirName)

object PathNameOf {

  implicit def fileNameOf[A: Arbitrary: Show]: Arbitrary[FileNameOf[A]] =
    Arbitrary(genFileName[A] map (FileNameOf(_)))

  implicit def dirNameOf[A: Arbitrary: Show]: Arbitrary[DirNameOf[A]] =
    Arbitrary(genDirName[A] map (DirNameOf(_)))

  ////

  private[scalacheck] def genFileName[A: Arbitrary: Show]: Gen[FileName] =
    genSegment[A].map(FileName(_))

  private[scalacheck] def genDirName[A: Arbitrary: Show]: Gen[DirName] =
    genSegment[A].map(DirName(_))

  private def genSegment[A: Arbitrary: Show]: Gen[String] =
    Arbitrary.arbitrary[A] map (_.shows)
}
