scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.http4s"     %% "http4s-blaze-server" % "0.15.7a",
  "org.http4s"     %% "http4s-blaze-client" % "0.15.7a",
  "org.http4s"     %% "http4s-circe"        % "0.15.7a",
  "org.http4s"     %% "http4s-dsl"          % "0.15.7a",
  "io.circe"       %% "circe-generic"       % "0.8.0",
  "io.circe"       %% "circe-literal"       % "0.8.0",
  "org.json4s"     %% "json4s-native"       % "3.5.2",
  "ch.qos.logback" %  "logback-classic"     % "1.2.1"
)