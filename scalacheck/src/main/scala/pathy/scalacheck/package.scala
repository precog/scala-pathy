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

import Path._

import org.scalacheck.Gen

package object scalacheck {
  type AbsFileOf[A] = PathOf[Abs,File,Sandboxed,A]
  type RelFileOf[A] = PathOf[Rel,File,Sandboxed,A]
  type AbsDirOf[A]  = PathOf[Abs,Dir,Sandboxed,A]
  type RelDirOf[A]  = PathOf[Rel,Dir,Sandboxed,A]

  /** Generator that distributes the available size to two component generators,
    * and then combines the results. Can be used to generate nested structures
    * where the aggregate size of component/leaf elements is effectively controlled
    * by the size parameter.
    */
  def sizeDistributed[A, B, C](ga: Gen[A], gb: Gen[B])(f: (A, B) => C): Gen[C] =
    for {
      n <- Gen.size
      x <- Gen.choose(0, n)
      a <- Gen.resize(x, ga)
      b <- Gen.resize(n - x, gb)
    } yield f(a, b)

  /** Generator for lists of non-atomic components, where the size parameter is
    * spread across all the generated elements so that the aggregate size of
    * component/leaf elements is effectively controlled by the size parameter.
    * No element is ever generated with a size parameter of less than 1, and 
    * each "cons cell" consumes one unit of size.
    */
  def sizeDistributedListOfNonEmpty[A](g: Gen[A]): Gen[List[A]] =
    Gen.size.flatMap { n =>
      if (n < 1) Gen.const(Nil)
      else if (n < 3) g.map(_ :: Nil)
      else
        for {
          l <- Gen.choose(1, n)
          r = n - l - 1
          h <- Gen.resize(l, g)
          t <- Gen.resize(r, sizeDistributedListOfNonEmpty(g))
        } yield (h :: t)
    }
}
