/*
 * Copyright 2014–2017 SlamData Inc.
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

package pathy.argonaut

import slamdata.Predef._
import pathy.Path, Path._
import pathy.scalacheck.PathyArbitrary._

import argonaut._, Argonaut._
import org.specs2.mutable
import org.specs2.ScalaCheck

final class PosixCodecJsonSpec extends mutable.Specification with ScalaCheck {
  import PosixCodecJson._

  "RelFile Codec" ! prop { rf: RelFile[Sandboxed] =>
    CodecJson.codecLaw(CodecJson.derived[RelFile[Unsandboxed]])(unsandbox(rf))
  }

  "RelDir Codec" ! prop { rd: RelDir[Sandboxed] =>
    CodecJson.codecLaw(CodecJson.derived[RelDir[Unsandboxed]])(unsandbox(rd))
  }

  "AbsFile Codec" ! prop { af: AbsFile[Sandboxed] =>
    CodecJson.codecLaw(CodecJson.derived[AbsFile[Sandboxed]])(af)
  }

  "AbsDir Codec" ! prop { ad: AbsDir[Sandboxed] =>
    CodecJson.codecLaw(CodecJson.derived[AbsDir[Sandboxed]])(ad)
  }

  "RelFile encode" ! prop { rf: RelFile[Sandboxed] =>
    rf.asJson ==== jString(posixCodec.printPath(rf))
  }

  "RelDir encode" ! prop { rd: RelDir[Sandboxed] =>
    rd.asJson ==== jString(posixCodec.printPath(rd))
  }

  "AbsFile encode" ! prop { af: AbsFile[Sandboxed] =>
    af.asJson ==== jString(posixCodec.printPath(af))
  }

  "AbsFile encode" ! prop { ad: AbsDir[Sandboxed] =>
    ad.asJson ==== jString(posixCodec.printPath(ad))
  }

  "Decode failure" ! prop { s: String =>
    (!s.isEmpty && !s.contains('\\') && !s.contains('"')) ==> {

    val jstr = s""""$s""""

    def decodeAs[X: DecodeJson]: Either[String, X] =
      jstr.decodeWithEither[Either[String, X], X](
        Right(_),
        e => Left(e.fold(s => s, _._1)))

    (decodeAs[RelDir[Unsandboxed]] must beLeft("RelDir[Unsandboxed]")) and
    (decodeAs[AbsDir[Sandboxed]] must beLeft("AbsDir[Sandboxed]"))
  }}
}
