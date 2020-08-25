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
package scalacheck

import org.scalacheck.Arbitrary

object PathyArbitrary {
  import Path._, PathNameOf._

  implicit val arbitraryAbsFile: Arbitrary[AbsFile[Sandboxed]] =
    arbPath[Abs,File,Sandboxed,RandomSeg]

  implicit val arbitraryRelFile: Arbitrary[RelFile[Sandboxed]] =
    arbPath[Rel,File,Sandboxed,RandomSeg]

  implicit val arbitraryAbsDir: Arbitrary[AbsDir[Sandboxed]] =
    arbPath[Abs,Dir,Sandboxed,RandomSeg]

  implicit val arbitraryRelDir: Arbitrary[RelDir[Sandboxed]] =
    arbPath[Rel,Dir,Sandboxed,RandomSeg]

  implicit val arbitraryFileName: Arbitrary[FileName] =
    Arbitrary(genFileName[RandomSeg])

  implicit val arbitraryDirName: Arbitrary[DirName] =
    Arbitrary(genDirName[RandomSeg])

  ////

  def arbPath[B,T,S,A](implicit GP: Arbitrary[PathOf[B,T,S,A]]): Arbitrary[Path[B,T,S]] =
    Arbitrary(GP.arbitrary map (_.path))
}
