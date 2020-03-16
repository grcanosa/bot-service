
organization := "com.grcanosa"
name in (ThisBuild, Compile) := "grcanosa-bots-multiproject"

scalaVersion := "2.12.8"

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .enablePlugins(PackagingTypePlugin)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    library,
    bot_grcanosa,
    bot_grupo,
    bot_renfe,
    bot_apps
  )

lazy val library = project
  .in(file("src/library"))
  .enablePlugins(PackagingTypePlugin)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "telegrambot",
    libraryDependencies ++= libDeps
  )



lazy val bot_grcanosa = project
  .in(file("src/bot_grcanosa"))
  .enablePlugins(PackagingTypePlugin)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "bot_grcanosa"
  )
  .dependsOn(
    library
  )



lazy val bot_grupo = project
  .in(file("src/bot_grupo"))
  .enablePlugins(PackagingTypePlugin)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "bot_grupo"
  )
  .dependsOn(
    library
  )



lazy val bot_renfe = project
  .in(file("src/bot_renfe"))
  .enablePlugins(PackagingTypePlugin)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "bot_renfe"
  )
  .dependsOn(
    library
  )

lazy val bot_apps = project
  .in(file("src/bot_apps"))
  .enablePlugins(PackagingTypePlugin)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "bot_apps",
    assemblySettings,
    artifact in (Compile, assembly):= {
      val art = (artifact in (Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in (Compile, assembly), assembly)
  )
  .dependsOn(
     bot_grcanosa
    , bot_grupo
  )

val telegramBotVersion = "4.0.0-RC2"
val seleniumVersion = "3.141.59"

val libDeps = Seq(
    "com.vdurmont" % "emoji-java" % "4.0.0",
    "com.bot4s" %% "telegram-core" % telegramBotVersion,
    "com.bot4s" %% "telegram-akka" % telegramBotVersion,
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
    "org.seleniumhq.selenium" % "selenium-support" % seleniumVersion,
    "org.seleniumhq.selenium" % "selenium-remote-driver" % seleniumVersion,
    "com.github.etaty" %% "rediscala" % "1.9.0",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "io.spray" %% "spray-json" % "1.3.5",
    "ch.qos.logback" % "logback-classic" % "1.1.7"
)

// val sparkVersion = "2.3.0"

// val commonDependencies = Seq(
//    "com.alstom.mastria" %% "mastria-avro-schemas" % "0.0.9"
//   , etcd4sCore
//   , typesafeScalaLogging
//   , logbackContribJackson
//   , logbackContribJsonClassic
//   , jacksonDatabind
//   , jacksonCore
//   , jacksonModule
//   , "net.logstash.logback" % "logstash-logback-encoder" %"6.1"
//   , logbackCore
//   , logbackClassic
//   , "org.json4s" %% "json4s-native" % "3.6.7"
//   , avro
//   , avroCompiler
//   , avroSerializerConfluent
//   , typesafeConfig
//   , kafka
//   , kafkaClient
//   , jodaConvert
//   , akka
//   , akkaHttp
//   , akkaStream
//   , alpakkaKafka
//   , "org.rogach" %% "scallop" % "3.3.1"
//   , sparkCore % "provided" excludeAll( ExclusionRule(organization = "org.slf4j"))
//   , "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided" excludeAll( ExclusionRule(organization = "org.slf4j"))
//   , sparkSql % "provided" excludeAll( ExclusionRule(organization = "org.slf4j"))
//   , "io.prometheus" % "simpleclient" % "0.8.0"
//   , "io.prometheus" % "simpleclient_pushgateway" % "0.8.0"
//   , "io.prometheus" % "simpleclient_httpserver" % "0.8.0"


// )

// val testDependencies= Seq(
//   scalaTest
//   , embeddedKafkaTest
//   , embeddedKafkaSchemaRegistryTest
//   , akkaStreamTest
// )




lazy val assemblySettings = Seq(
assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}-assembly.jar",
// Skip the tests (comment out to run the tests).
test in assembly := {},

assemblyMergeStrategy in assembly := {
  // Needed only to sbt assembly non provided spark-streaming-kafka-0-10
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  // Needed only to sbt assembly etcd coming with libraries
  case PathList("META-INF", "io.netty.versions.properties", xs @ _*) => MergeStrategy.last
  // Needed only to sbt assembly mastria-etcd4s coming with libraries
  case PathList("scala","collection","mutable", xs @ _*) => MergeStrategy.first
  case PathList("scala","util", xs @ _*) => MergeStrategy.first
  case PathList("library.properties", xs @ _*) => MergeStrategy.first
  case PathList("logback.xml",xs @ _ *) => MergeStrategy.last
  case x => (assemblyMergeStrategy in assembly).value(x)
}
,logLevel in assembly := Level.Info
)


// The default SBT testing java options are too small to support running many
// of the tests due to the need to launch Spark in local mode.
parallelExecution in Test := false
fork in Test := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

updateOptions := updateOptions.value.withLatestSnapshots(false)


pomExtra := {
  <scm>
    <url>https://github.com/AlstomDigitalMobility/mastria-common-libraries</url>
    <connection>https://github.com/AlstomDigitalMobility/mastria-common-libraries.git</connection>
  </scm>
    <developers>
      <developer>
        <id>grcanosa@gmail.com</id>
        <name>Gonzalo Rodriguez</name>
        <url>https://github.com/gonzalo-rodriguez-alstom</url>
      </developer>
    </developers>
}
