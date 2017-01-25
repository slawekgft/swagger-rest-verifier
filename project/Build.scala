

object MyBuild extends Build {

  val root = play.Project("swagger-rest-validator", path = file("./"))
    .settings(
      version := Pom.version(baseDirectory.value),
      libraryDependencies ++= Pom.dependencies(baseDirectory.value))

  override def rootProject = Some(root)
}