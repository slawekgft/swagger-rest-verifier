package com.gft.lr.restcheck

import java.nio.file.Path
import java.util.function.Function
import java.util.stream.Stream

import swagger.JsonPreparator

import scala.collection.JavaConverters._

/**
  * Created on 09/01/17.
  */
class RESTSpecValidator(private val contractsPath: String, private val swaggerBuilder: SwaggerBuilder, private val jsonPreparator: JsonPreparator, private val restVerifierConf:RESTVerifierConf) {

  private lazy val ignoredPaths: Seq[String] = restVerifierConf.readIngnoreConfiguration(swaggerBuilder.getRESTSpecsRelativePath + RESTVerifierConf.VALIDATORIGNORE).asScala.toSeq;

  private val stringToStream: Function[String, Stream[Path]] = (path: String) => RESTSpecLRValidator.getPathsOf(path)

  def swaggerResources: java.util.List[SwaggerResource] =
    swaggerBuilder
      .prepareSwaggers(stringToStream,
        (p: Path) => notIgnored(p)).asScala.toList
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
