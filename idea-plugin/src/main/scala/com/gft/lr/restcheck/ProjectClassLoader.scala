package com.gft.lr.restcheck

import java.io.InputStream
import java.util.jar.JarFile

import scala.util.control.Exception

/**
  * Created on 08/02/17.
  */
class ProjectClassLoader(val parent: ClassLoader, val jarFile: String) extends ClassLoader(parent) {
  var classes: Map[String, Class[_]] = Map()

  @throws(classOf[ClassNotFoundException])
  override def loadClass(className: String): Class[_] =
    Option(findClass(className)) match {
      case Some(classFound) => classFound
      case None => null
    }

  def readAndDefineClass(stream: InputStream, name: String): Option[Class[_]] = {
    val classBytes = closeAfterReading(stream) {
      is => Stream.continually(is.read()).takeWhile(-1 != _).map(_.toByte).toArray
    }
    try {
      val result = defineClass(name, classBytes, 0, classBytes.length, null)
      classes = classes + (name -> result)
      Some(result)
    } catch {
      case e: Exception => {
        e.printStackTrace();
        null
      }
    }
  }

  override def findClass(name: String): Class[_] = {
    val className = name.replaceAll("\\.", "/");
    classes.get(name) match {
      case Some(classObj) => classObj
      case None => try {
        findSystemClass(name)
      } catch {
        case cnf: ClassNotFoundException => {
          val jar: JarFile = new JarFile(jarFile)
          Option(jar.getJarEntry(className + ".class"))
            .map(entry => jar.getInputStream(entry))
            .flatMap(readAndDefineClass(_, name))
            .get
        }
      }
    }
  }

  def closeAfterReading(c: InputStream)(f: InputStream => Array[Byte]) = {
    Exception.catching(classOf[ClassNotFoundException])
      .andFinally(c.close())
      .either(f(c)) match {
      case Left(e) => {
        val msg = s"IO error reading file stream"
        new Array[Byte](0)
      }
      case Right(v) => v
    }
  }

}
