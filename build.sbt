import play.PlayImport.PlayKeys.playRunHooks
import scalariform.formatter.preferences._

organization := "com.github.edgecaseberg"

name := "resumable-play-uploads"
 
version := "0.0.0"

scalaVersion := "2.11.5"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
	"org.scalatestplus" %% "play" % "1.2.0" % "test",
	"com.bionicspirit" %% "shade" % "1.7.2"
)

scalariformPreferences := scalariformPreferences.value
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
  .setPreference(AlignParameters, false)
  .setPreference(IndentWithTabs, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)

fork in Test := true
