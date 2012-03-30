package com.petebevin.markdown

import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

class LinkDefinition(@BeanProperty var url: String, @BeanProperty var title: String)
    {

  override def toString(): String = url + " (" + title + ")"
}
