package com.gft.lr.restcheck

import io.swagger.util.Json
import play.api.Application
import play.api.inject.ApplicationLifecycle
import play.modules.swagger.{PlayReader, SwaggerPluginImpl}
import play.routes.compiler.Route

/**
  * Created on 26/01/17.
  */
class JsonPreparator(val lifecycle: ApplicationLifecycle,
                     val application: Application,
                     val projectClassLoader: ClassLoader) {

  val swaggerPlugin: SwaggerPluginImpl = new SwaggerPluginImpl(lifecycle, null, application)

  def loadClass(controllerRoute: Route) = this.projectClassLoader.loadClass(
      controllerRoute.call.packageName + "." + controllerRoute.call.controller)

  def prepareJson(swaggerResource: SwaggerResource): SwaggerResource = {
    val routes: List[Route] = swaggerPlugin.routes
    val routesOfApi: List[Route] =
      swaggerPlugin.routes
        .collect { case route
          if (route.call.packageName, route.call.controller, route.call.method) ==
            ("controllers", "ApiHelpController", "getResource") => route
        }

    val controllerResourceUrl = routesOfApi
      .find(route => swaggerResource.getUrl.endsWith(route.path.toString()))
    match {
      case Some(route) => route.call.parameters.get.head.fixed
      case None => None
    }

    val controllerRoute = controllerResourceUrl.getOrElse("").replaceAll("\"", "") match {
      case path => swaggerPlugin.routes
        .filter(r => r.path.toString().startsWith("rest/"))
        .find(rs => {
        println("path=" + path + ", rs=" + rs.path.toString());
        rs.path.toString().contains(path)
      })
      case "" => None
    }

    println("-->" + controllerRoute)

    controllerRoute match {
      case None => swaggerResource
      case Some(route) => {
        val playReader: PlayReader = new PlayReader(null)
        new SwaggerResource(Json.pretty(playReader.read(loadClass(route))), swaggerResource)
      }
    }
  }
}
