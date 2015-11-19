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

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scalaz.Show

final case class RandomStr(str: String) extends AnyVal

object RandomStr {
  implicit val randomStrArbitrary: Arbitrary[RandomStr] =
    Arbitrary {
      Gen.nonEmptyListOf(Gen.frequency(
        100 -> Arbitrary.arbitrary[Char],
         10 -> Gen.const('.'),
         10 -> Gen.const('/')
      )) map (cs => RandomStr(cs.mkString))
    }

  implicit val randomStrShow: Show[RandomStr] =
    Show.shows(_.str)
}
