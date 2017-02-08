package com.gft.lr.restcheck

import java.nio.file.Path
import java.util.function.{Function, Predicate}
import java.util.stream.Stream

import scala.collection.JavaConverters._

/**
  * Created on 09/01/17.
  */
class RESTSpecValidator(private val contractsPath: String, private val swaggerBuilder: SwaggerBuilder, private val jsonPreparator: JsonPreparator, private val restVerifierConf: RESTVerifierConf) {

  private lazy val ignoredPaths: Seq[String] = restVerifierConf.readIngnoreConfiguration(swaggerBuilder.getRESTSpecsRelativePath + RESTVerifierConf.VALIDATORIGNORE).asScala.toSeq;

  private val stringToStream: Function[String, Stream[Path]] =
    new Function[String, Stream[Path]] {
      override def apply(path: String): Stream[Path] = RESTSpecLRValidator.getPathsOf(path)
    }


  def swaggerResources: java.util.List[SwaggerResource] =
    swaggerBuilder
      .prepareSwaggers(stringToStream,
        new Predicate[Path] {
          override def test(path: Path): Boolean = notIgnored(path)
        }).asScala.toList
      .map((sr: SwaggerResource) => jsonPreparator.prepareJson(sr))
      .asJava;

  private def prepareJSons(swaggerResource: SwaggerResource) = new SwaggerResource("", swaggerResource)

  private def notIgnored(path: Path): Boolean = {
    if (ignoredPaths.isEmpty)
      true
    else
      !(ignoredPaths
        .map((p) => path.toString().contains(p))
        .reduce(_ || _))
  }
}
