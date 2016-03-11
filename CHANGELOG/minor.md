Fix issues with `posixCodec` and `windowsCodec` when parsing paths with `.` and `..` in path segments

- Create new subproject `specs2` in order to publish a function (`validateIsLossless`) so that external librairies can validate their own `PathCodec`