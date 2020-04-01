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

import slamdata.Predef._

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scalaz.Show

/** Newtype for path segment strings with a generator that produces mostly
  * alphanumeric, then any printable ASCII char, with slightly more `.` and `/`
  * characters (because they tend to be problematic for encoders), and finally
  * an occasional char from anywhere in Unicode. */
private[scalacheck] final case class RandomSeg(str: String) extends AnyVal

private[scalacheck] object RandomSeg {
  implicit val arbitrary: Arbitrary[RandomSeg] =
    Arbitrary {
      Gen.nonEmptyListOf(Gen.frequency(
        50 -> Gen.alphaChar,
        25 -> Gen.choose(MinPrintableASCII, MaxPrintableASCII),
        10 -> Gen.const('.'),
        10 -> Gen.const('/'),
         5 -> Arbitrary.arbitrary[Char]
      )) map (cs => RandomSeg(cs.mkString))
    }

  implicit val show: Show[RandomSeg] =
    Show.shows(_.str)
}
