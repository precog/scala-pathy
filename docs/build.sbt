import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._

site.settings

tutSettings

site.addMappingsToSiteDir(tut, "tut")

unidocSettings

site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api")

ghpages.settings

ghpagesNoJekyll := false

includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"

git.remoteRepo := "git@github.com:slamdata/scala-pathy.git"
