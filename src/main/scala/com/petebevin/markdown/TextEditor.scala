package com.petebevin.markdown

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.Collection
import java.util.List
import java.util.ArrayList
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Mutable String with common operations used in Markdown processing.
 */
class TextEditor(pText: CharSequence) {

  private var text: StringBuffer = new StringBuffer(pText.toString)

  /**
   * Give up the contents of the TextEditor.
   * @return
   */
  override def toString(): String = text.toString

  /**
   * Replace all occurrences of the regular expression with the replacement.  The replacement string
   * can contain $1, $2 etc. referring to matched groups in the regular expression.
   *
   * @param regex
   * @param replacement
   * @return
   */
  def replaceAll(regex: String, replacement: String): TextEditor = {
    if (text.length > 0) {
      val r = replacement
      val p = Pattern.compile(regex, Pattern.MULTILINE)
      val m = p.matcher(text)
      val sb = new StringBuffer()
      while (m.find()) {
        m.appendReplacement(sb, r)
      }
      m.appendTail(sb)
      text = sb
    }
    this
  }

  /**
   * Same as replaceAll(String, String), but does not interpret
   * $1, $2 etc. in the replacement string.
   * @param regex
   * @param replacement
   * @return
   */
  def replaceAllLiteral(regex: String, pReplacement: String): TextEditor = {
    replaceAll(Pattern.compile(regex, Pattern.MULTILINE), new Replacement() {

      def replacement(m: Matcher): String = pReplacement
    })
  }

  /**
   * Replace all occurrences of the Pattern.  The Replacement object's replace() method is
   * called on each match, and it provides a replacement, which is placed literally
   * (i.e., without interpreting $1, $2 etc.)
   *
   * @param pattern
   * @param replacement
   * @return
   */
  def replaceAll(pattern: Pattern, replacement: Replacement): TextEditor = {
    val m = pattern.matcher(text)
    var lastIndex = 0
    val sb = new StringBuffer()
    while (m.find()) {
      sb.append(text.subSequence(lastIndex, m.start()))
      sb.append(replacement.replacement(m))
      lastIndex = m.end()
    }
    sb.append(text.subSequence(lastIndex, text.length))
    text = sb
    this
  }

  /**
   * Remove all occurrences of the given regex pattern, replacing them
   * with the empty string.
   *
   * @param pattern Regular expression
   * @return
   * @see java.util.regex.Pattern
   */
  def deleteAll(pattern: String): TextEditor = replaceAll(pattern, "")

  /**
   * Convert tabs to spaces given the default tab width of 4 spaces.
   * @return
   */
  def detabify(): TextEditor = detabify(4)

  /**
   * Convert tabs to spaces.
   *
   * @param tabWidth  Number of spaces per tab.
   * @return
   */
  def detabify(tabWidth: Int): TextEditor = {
    replaceAll(Pattern.compile("(.*?)\\t"), new Replacement() {

      def replacement(m: Matcher): String = {
        val lineSoFar = m.group(1)
        var width = lineSoFar.length
        val replacement = new StringBuffer(lineSoFar)
        do {
          replacement.append(' ')
          width
        } while (width % tabWidth != 0);
        return replacement.toString
      }
    })
    this
  }

  /**
   * Remove a number of spaces at the start of each line.
   * @param spaces
   * @return
   */
  def outdent(spaces: Int): TextEditor = {
    deleteAll("^(\\t|[ ]{1," + spaces + "})")
  }

  /**
   * Remove one tab width (4 spaces) from the start of each line.
   * @return
   */
  def outdent(): TextEditor = outdent(4)

  /**
   * Remove leading and trailing space from the start and end of the buffer.  Intermediate
   * lines are not affected.
   * @return
   */
  def trim(): TextEditor = {
    text = new StringBuffer(text.toString.trim())
    this
  }

  /**
   * Introduce a number of spaces at the start of each line.
   * @param spaces
   * @return
   */
  def indent(spaces: Int): TextEditor = {
    val sb = new StringBuffer(spaces)
    for (i <- 0 until spaces) {
      sb.append(' ')
    }
    replaceAll("^", sb.toString)
  }

  /**
   * Add a string to the end of the buffer.
   * @param s
   */
  def append(s: CharSequence) {
    text.append(s)
  }

  /**
   * Parse HTML tags, returning a Collection of HTMLToken objects.
   * @return
   */
  def tokenizeHTML(): Collection[HTMLToken] = {
    val tokens = new ArrayList[HTMLToken]()
    val nestedTags = nestedTagsRegex(6)
    val p = Pattern.compile("" + "(?s:<!(--.*?--\\s*)+>)" + "|" + "(?s:<\\?.*?\\?>)" + 
      "|" + 
      nestedTags + 
      "", Pattern.CASE_INSENSITIVE)
    val m = p.matcher(text)
    var lastPos = 0
    while (m.find()) {
      if (lastPos < m.start()) {
        tokens.add(HTMLToken.text(text.substring(lastPos, m.start())))
      }
      tokens.add(HTMLToken.tag(text.substring(m.start(), m.end())))
      lastPos = m.end()
    }
    if (lastPos < text.length) {
      tokens.add(HTMLToken.text(text.substring(lastPos, text.length)))
    }
    tokens
  }

  /**
   * Regex to match a tag, possibly with nested tags such as <a href="<MTFoo>">.
   *
   * @param depth - How many levels of tags-within-tags to allow.  The example <a href="<MTFoo>"> has depth 2.
   */
  private def nestedTagsRegex(depth: Int): String = {
    if (depth == 0) {
      ""
    } else {
      "(?:<[a-z/!$](?:[^<>]|" + nestedTagsRegex(depth - 1) + 
        ")*>)"
    }
  }

  /**
   * Add a string to the start of the first line of the buffer.
   * @param s
   */
  def prepend(s: CharSequence) {
    val newText = new StringBuffer()
    newText.append(s)
    newText.append(text)
    text = newText
  }

  /**
   * Find out whether the buffer is empty.
   * @return
   */
  def isEmpty(): Boolean = text.length == 0
}
