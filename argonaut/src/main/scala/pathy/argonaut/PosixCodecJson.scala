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

package pathy.argonaut

import slamdata.Predef._
import pathy.Path, Path._

import argonaut._, Argonaut._

object PosixCodecJson {
  import posixCodec._

  implicit val relFileDecodeJson: DecodeJson[RelFile[Unsandboxed]] =
    decodeRelPath("File", parseRelFile)

  implicit val relDirDecodeJson: DecodeJson[RelDir[Unsandboxed]] =
    decodeRelPath("Dir", parseRelDir)

  implicit val absFileDecodeJson: DecodeJson[AbsFile[Sandboxed]] =
    decodeAbsPath("File", parseAbsFile)

  implicit val absDirDecodeJson: DecodeJson[AbsDir[Sandboxed]] =
    decodeAbsPath("Dir", parseAbsDir)

  implicit def pathEncodeJson[B, T, S]: EncodeJson[Path[B, T, S]] =
    EncodeJson.of[String].contramap(unsafePrintPath)

  ////

  private def decodeRelPath[T](
    typeName: String,
    parse: String => Option[Path[Rel, T, Unsandboxed]]
  ): DecodeJson[Path[Rel, T, Unsandboxed]] =
    DecodeJson.of[String] flatMap { str =>
      DecodeJson { cur => parse(str) match {
        case Some(path) => DecodeResult.ok(path)
        case None       => DecodeResult.fail(s"Rel${typeName}[Unsandboxed]", cur.history)
      }}
    }

  private def decodeAbsPath[T](
    typeName: String,
    parse: String => Option[Path[Abs, T, Unsandboxed]]
  ): DecodeJson[Path[Abs, T, Sandboxed]] =
    DecodeJson.of[String] flatMap { str =>
      DecodeJson { cur => parse(str).flatMap(sandbox(rootDir, _)) match {
        case Some(path) => DecodeResult.ok(rootDir </> path)
        case None       => DecodeResult.fail(s"Abs${typeName}[Sandboxed]", cur.history)
      }}
    }
}
