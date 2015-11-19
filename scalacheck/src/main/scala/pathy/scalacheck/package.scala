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

package object scalacheck {
  type GenAbsFile[A] = GenPath[Abs,File,Sandboxed,A]
  type GenRelFile[A] = GenPath[Rel,File,Sandboxed,A]
  type GenAbsDir[A]  = GenPath[Abs,Dir,Sandboxed,A]
  type GenRelDir[A]  = GenPath[Rel,Dir,Sandboxed,A]
}
