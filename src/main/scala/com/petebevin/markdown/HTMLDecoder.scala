package com.petebevin.markdown

import java.util.regex.Matcher
import java.util.regex.Pattern
//remove if not needed
import scala.collection.JavaConversions._

object HTMLDecoder {

  def decode(html: String): String = {
    val ed = new TextEditor(html)
    val p1 = Pattern.compile("&#(\\d+);")
    ed.replaceAll(p1, new Replacement() {

      def replacement(m: Matcher): String = {
        val charDecimal = m.group(1)
        val ch = Integer.parseInt(charDecimal).toChar
        return Character toString ch
      }
    })
    val p2 = Pattern.compile("&#x([0-9a-fA-F]+);")
    ed.replaceAll(p2, new Replacement() {

      def replacement(m: Matcher): String = {
        val charHex = m.group(1)
        val ch = Integer.parseInt(charHex, 16).toChar
        return Character toString ch
      }
    })
    ed.toString
  }
}
