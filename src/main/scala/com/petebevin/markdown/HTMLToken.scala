package com.petebevin.markdown

import HTMLToken._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

object HTMLToken {

  def tag(text: String): HTMLToken = new HTMLToken(true, text)

  def text(text: String): HTMLToken = new HTMLToken(false, text)
}

class HTMLToken private (var isTag: Boolean, @BeanProperty var text: String)
    {

  override def toString(): String = {
    var `type`: String = ""
    `type` = if (isTag) "tag" else "text"
    `type` + ": " + getText
  }
}
