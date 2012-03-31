package com.petebevin.markdown

import java.util.regex.Matcher
//remove if not needed
import _root_.scala.collection.JavaConversions._

trait Replacement {

  def replacement(m: Matcher): String
}
