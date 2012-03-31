package com.petebevin.markdown

import _root_.scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import _root_.scala.collection.JavaConversions._

class LinkDefinition(@BeanProperty var url: String, @BeanProperty var title: String)
    {

  override def toString(): String = url + " (" + title + ")"
}
