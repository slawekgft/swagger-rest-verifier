package com.gft.swagger

import java.nio.file.Path

import com.gft.lr.restcheck.{RESTSpecLRValidator, RESTVerifierConfUtil, SwaggerBuilder, SwaggerResource}

/**
  * Created on 09/01/17.
  */
class FacadeContracts(val path: String, val swaggerBuilder: SwaggerBuilder) {

  lazy val ignoredPaths: Set[String] = RESTVerifierConfUtil.readIngnoreConfiguration(swaggerBuilder.getRESTSpecsRelativePath + RESTVerifierConfUtil.VALIDATORIGNORE);

  val swaggerResources: Array[SwaggerResource] =
    swaggerBuilder.prepareSwaggers(_ => RESTSpecLRValidator.getPathsOf(_), _ => notIgnored(_))
      .map(_ => prepareJSons(_));

  def private prepareJSons(swagger: SwaggerResource) = new SwaggerResource("", swaggerResource)

  def notIgnored(path: Path) =
    ignoredPaths
      .map((p) => path.toString.contains p)
      .reduce((_, _) => _ && _)
}
