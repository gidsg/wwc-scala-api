name := "WWC Scala Tutorial"
version := "1.0"
scalaVersion := "2.12.1"

val Http4sVersion = "0.15.7a"

libraryDependencies ++= Seq(
  "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"     %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"     %% "http4s-circe"        % Http4sVersion,
  "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
  "ch.qos.logback" %  "logback-classic"     % "1.2.1",
  "io.circe" %% "circe-generic" % "0.7.0"
)