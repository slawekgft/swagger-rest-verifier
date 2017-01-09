package com.gft.swagger

import com.gft.lr.restcheck.{SwaggerBuilder, SwaggerResource}

/**
  * Created on 09/01/17.
  */
class FacadeContracts(val path:String, val swaggerBuilder:SwaggerBuilder) {
  val swaggers : Array[SwaggerResource];

}
