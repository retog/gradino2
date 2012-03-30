package com.petebevin.markdown

import java.util.Collection
import java.util.Map
import java.util.Random
import java.util.TreeMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import MarkdownProcessor._
//remove if not needed
import scala.collection.JavaConversions._

object MarkdownProcessor {

  private val HTML_PROTECTOR = new CharacterProtector()

  private val CHAR_PROTECTOR = new CharacterProtector()

  def main(args: Array[String]) {
    val buf = new StringBuffer()
    val cbuf = new Array[Char](1024)
    val in = new java.io.InputStreamReader(System.in)
    try {
      var charsRead = in.read(cbuf)
      while (charsRead >= 0) {
        buf.append(cbuf, 0, charsRead)
        charsRead = in.read(cbuf)
      }
      println(new MarkdownProcessor().markdown(buf.toString))
    } catch {
      case e: java.io.IOException => {
        System.err.println("Error reading input: " + e.getMessage)
        System.exit(1)
      }
    }
  }
}

/**
 * Convert Markdown text into HTML, as per http://daringfireball.net/projects/markdown/ .
 * Usage:
 * <pre><code>
 *     MarkdownProcessor markdown = new MarkdownProcessor();
 *     String html = markdown.markdown("*italic*   **bold**\n_italic_   __bold__");
 * </code></pre>
 */
class MarkdownProcessor {

  private var rnd: Random = new Random()

  private var linkDefinitions: Map[String, LinkDefinition] = new TreeMap[String, LinkDefinition]()

  private var listLevel: Int = 0

  private var emptyElementSuffix: String = " />"

  private var tabWidth: Int = 4

  /**
   * Perform the conversion from Markdown to HTML.
   *
   * @param txt - input in markdown format
   * @return HTML block corresponding to txt passed in.
   */
  def markdown(pTxt: String): String = {
    val txt = if (pTxt == null) {
      ""
    } else pTxt
    var text = new TextEditor(txt)
    text.replaceAll("\\r\\n", "\n")
    text.replaceAll("\\r", "\n")
    text.replaceAll("^[ \\t]+$", "")
    text.append("\n\n")
    text.detabify()
    text.deleteAll("^[ ]+$")
    hashHTMLBlocks(text)
    stripLinkDefinitions(text)
    text = runBlockGamut(text)
    unEscapeSpecialChars(text)
    text.append("\n")
    text.toString
  }

  private def encodeBackslashEscapes(text: TextEditor): TextEditor = {
    val normalChars = "`_>!".toCharArray()
    val escapedChars = "*{}[]()#+-.".toCharArray()
    text.replaceAllLiteral("\\\\\\\\", CHAR_PROTECTOR.encode("\\"))
    encodeEscapes(text, normalChars, "\\\\")
    encodeEscapes(text, escapedChars, "\\\\\\")
    text
  }

  private def encodeEscapes(text: TextEditor, chars: Array[Char], slashes: String): TextEditor = {
    for (ch <- chars) {
      val regex = slashes + ch
      text.replaceAllLiteral(regex, CHAR_PROTECTOR.encode(String.valueOf(ch)))
    }
    text
  }

  private def stripLinkDefinitions(text: TextEditor) {
    val p = Pattern.compile("^[ ]{0,3}\\[(.+)\\]:" + "[ \\t]*\\n?[ \\t]*" + "<?(\\S+?)>?" + 
      "[ \\t]*\\n?[ \\t]*" + 
      "(?:[\"(](.+?)[\")][ \\t]*)?" + 
      "(?:\\n+|\\Z)", Pattern.MULTILINE)
    text.replaceAll(p, new Replacement() {

      def replacement(m: Matcher): String = {
        val id = m.group(1).toLowerCase()
        val url = encodeAmpsAndAngles(new TextEditor(m.group(2))).toString
        var title = m.group(3)
        if (title == null) {
          title = ""
        }
        title = replaceAll(title, "\"", "&quot;")
        linkDefinitions.put(id, new LinkDefinition(url, title))
        ""
      }
    })
  }

  def runBlockGamut(text: TextEditor): TextEditor = {
    doHeaders(text)
    doHorizontalRules(text)
    doLists(text)
    doCodeBlocks(text)
    doBlockQuotes(text)
    hashHTMLBlocks(text)
    formParagraphs(text)
  }

  private def doHorizontalRules(text: TextEditor) {
    val hrDelimiters = Array("\\*", "-", "_")
    for (hrDelimiter <- hrDelimiters) {
      text.replaceAll("^[ ]{0,2}([ ]?" + hrDelimiter + "[ ]?){3,}[ ]*$", "<hr />")
    }
  }

  private def hashHTMLBlocks(text: TextEditor) {
    val tagsA = Array("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "pre", "table", "dl", "ol", "ul", "script", "noscript", "form", "fieldset", "iframe", "math")
    val tagsB = Array("ins", "del")
    val alternationA = join("|", tagsA)
    val alternationB = alternationA + "|" + join("|", tagsB)
    val less_than_tab = tabWidth - 1
    val p1 = Pattern.compile("(" + "^<(" + alternationA + ")" + "\\b" + "(.*\\n)*?" + 
      "</\\2>" + 
      "[ ]*" + 
      "(?=\\n+|\\Z))", Pattern.MULTILINE)
    val protectHTML = new Replacement() {

      def replacement(m: Matcher): String = {
        var literal = m.group()
        return "\n\n" + HTML_PROTECTOR.encode(literal) + "\n\n"
      }
    }
    text.replaceAll(p1, protectHTML)
    val p2 = Pattern.compile("(" + "^" + "<(" + alternationB + ")" + "\\b" + "(.*\\n)*?" + 
      ".*</\\2>" + 
      "[ ]*" + 
      "(?=\\n+|\\Z))", Pattern.MULTILINE)
    text.replaceAll(p2, protectHTML)
    val p3 = Pattern.compile("(?:" + "(?<=\\n\\n)" + "|" + "\\A\\n?" + ")" + "(" + 
      "[ ]{0," + 
      less_than_tab + 
      "}" + 
      "<(hr)" + 
      "\\b" + 
      "([^<>])*?" + 
      "/?>" + 
      "[ ]*" + 
      "(?=\\n{2,}|\\Z))")
    text.replaceAll(p3, protectHTML)
    val p4 = Pattern.compile("(?:" + "(?<=\\n\\n)" + "|" + "\\A\\n?" + ")" + "(" + 
      "[ ]{0," + 
      less_than_tab + 
      "}" + 
      "(?s:" + 
      "<!" + 
      "(--.*?--\\s*)+" + 
      ">" + 
      ")" + 
      "[ ]*" + 
      "(?=\\n{2,}|\\Z)" + 
      ")")
    text.replaceAll(p4, protectHTML)
  }

  private def formParagraphs(markup: TextEditor): TextEditor = {
    markup.deleteAll("\\A\\n+")
    markup.deleteAll("\\n+\\z")
    var paragraphs: Array[String] = if (markup.isEmpty) new Array[String](0) else Pattern.compile("\\n{2,}").split(markup.toString)
    for (i <- 0 until paragraphs.length) {
      var paragraph = paragraphs(i)
      val decoded = HTML_PROTECTOR.decode(paragraph)
      if (decoded != null) {
        paragraphs(i) = decoded
      } else {
        paragraph = runSpanGamut(new TextEditor(paragraph)).toString
        paragraphs(i) = "<p>" + paragraph + "</p>"
      }
    }
    new TextEditor(join("\n\n", paragraphs))
  }

  private def doAutoLinks(markup: TextEditor): TextEditor = {
    markup.replaceAll("<((https?|ftp):[^'\">\\s]+)>", "<a href=\"$1\">$1</a>")
    val email = Pattern.compile("<([-.\\w]+\\@[-a-z0-9]+(\\.[-a-z0-9]+)*\\.[a-z]+)>")
    markup.replaceAll(email, new Replacement() {

      def replacement(m: Matcher): String = {
        val address = m.group(1)
        val ed = new TextEditor(address)
        unEscapeSpecialChars(ed)
        val addr = encodeEmail(ed.toString)
        val url = encodeEmail("mailto:" + ed.toString)
        return "<a href=\"" + url + "\">" + addr + "</a>"
      }
    })
    markup
  }

  private def unEscapeSpecialChars(ed: TextEditor) {
    for (hash <- CHAR_PROTECTOR.getAllEncodedTokens) {
      val plaintext = CHAR_PROTECTOR.decode(hash)
      ed.replaceAllLiteral(hash, plaintext)
    }
  }

  private def encodeEmail(s: String): String = {
    val sb = new StringBuffer()
    val email = s.toCharArray()
    for (ch <- email) {
      val r = rnd.nextDouble()
      if (r < 0.45) {
        sb.append("&#")
        sb.append(ch.toInt)
        sb.append(';')
      } else if (r < 0.9) {
        sb.append("&#x")
        sb.append(Integer.toString(ch.toInt, 16))
        sb.append(';')
      } else {
        sb.append(ch)
      }
    }
    sb.toString
  }

  private def doBlockQuotes(markup: TextEditor): TextEditor = {
    val p = Pattern.compile("(" + "(" + "^[ \t]*>[ \t]?" + ".+\\n" + "(.+\\n)*" + 
      "\\n*" + 
      ")+" + 
      ")", Pattern.MULTILINE)
    markup.replaceAll(p, new Replacement() {

      def replacement(m: Matcher): String = {
        var blockQuote = new TextEditor(m.group(1))
        blockQuote.deleteAll("^[ \t]*>[ \t]?")
        blockQuote.deleteAll("^[ \t]+$")
        blockQuote = runBlockGamut(blockQuote)
        blockQuote.replaceAll("^", "  ")
        val p1 = Pattern.compile("(\\s*<pre>.*?</pre>)", Pattern.DOTALL)
        blockQuote = blockQuote.replaceAll(p1, new Replacement() {

          def replacement(m1: Matcher): String = {
            var pre = m1.group(1)
            return deleteAll(pre, "^  ")
          }
        })
        "<blockquote>\n" + blockQuote + "\n</blockquote>\n\n"
      }
    })
  }

  private def doCodeBlocks(markup: TextEditor): TextEditor = {
    val p = Pattern.compile("" + "(?:\\n\\n|\\A)" + "((?:" + "(?:[ ]{4})" + ".*\\n+" + 
      ")+" + 
      ")" + 
      "((?=^[ ]{0,4}\\S)|\\Z)", Pattern.MULTILINE)
    markup.replaceAll(p, new Replacement() {

      private val LANG_IDENTIFIER = "lang:"

      def replacement(m: Matcher): String = {
        val codeBlock = m.group(1)
        val ed = new TextEditor(codeBlock)
        ed.outdent()
        encodeCode(ed)
        ed.detabify().deleteAll("\\A\\n+").deleteAll("\\s+\\z")
        val text = ed.toString
        var out = ""
        val fLine = firstLine(text)
        out = if (isLanguageIdentifier(fLine)) languageBlock(fLine, text) else genericCodeBlock(text)
        out
      }

      def firstLine(text: String): String = {
        if (text == null) return ""
        val splitted = text.split("\\n")
        splitted(0)
      }

      def isLanguageIdentifier(line: String): Boolean = {
        if (line == null) return false
        var lang = ""
        if (line.startsWith(LANG_IDENTIFIER)) {
          lang = line.replaceFirst(LANG_IDENTIFIER, "").trim()
        }
        lang.length > 0
      }

      def languageBlock(firstLine: String, text: String): String = {
        val codeBlockTemplate = "\n\n<pre class=\"%s\">\n%s\n</pre>\n\n"
        val lang = firstLine.replaceFirst(LANG_IDENTIFIER, "").trim()
        val block = text.replaceFirst(firstLine + "\n", "")
        String.format(codeBlockTemplate, lang, block)
      }

      def genericCodeBlock(text: String): String = {
        val codeBlockTemplate = "\n\n<pre><code>%s\n</code></pre>\n\n"
        String.format(codeBlockTemplate, text)
      }
    })
  }

  private def encodeCode(ed: TextEditor) {
    ed.replaceAll("&", "&amp;")
    ed.replaceAll("<", "&lt;")
    ed.replaceAll(">", "&gt;")
    ed.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
    ed.replaceAll("_", CHAR_PROTECTOR.encode("_"))
    ed.replaceAll("\\{", CHAR_PROTECTOR.encode("{"))
    ed.replaceAll("\\}", CHAR_PROTECTOR.encode("}"))
    ed.replaceAll("\\[", CHAR_PROTECTOR.encode("["))
    ed.replaceAll("\\]", CHAR_PROTECTOR.encode("]"))
    ed.replaceAll("\\\\", CHAR_PROTECTOR.encode("\\"))
  }

  private def doLists(text: TextEditor): TextEditor = {
    val lessThanTab = tabWidth - 1
    val wholeList = "(" + "(" + "[ ]{0," + lessThanTab + "}" + "((?:[-+*]|\\d+[.]))" + 
      "[ ]+" + 
      ")" + 
      "(?s:.+?)" + 
      "(" + 
      "\\z" + 
      "|" + 
      "\\n{2,}" + 
      "(?=\\S)" + 
      "(?![ ]*" + 
      "(?:[-+*]|\\d+[.])" + 
      "[ ]+" + 
      ")" + 
      ")" + 
      ")"
    if (listLevel > 0) {
      val replacer = new Replacement() {

        def replacement(m: Matcher): String = {
          var list = m.group(1)
          var listStart = m.group(3)
          var listType = ""
          listType = if (listStart.matches("[*+-]")) "ul" else "ol"
          list = replaceAll(list, "\\n{2,}", "\n\n\n")
          var result = processListItems(list)
          result = result.replaceAll("\\s+$", "")
          var html = if ("ul" == listType) "<ul>" + result + "</ul>\n" else "<ol>" + result + "</ol>\n"
          return html
        }
      }
      val matchStartOfLine = Pattern.compile("^" + wholeList, Pattern.MULTILINE)
      text.replaceAll(matchStartOfLine, replacer)
    } else {
      val replacer = new Replacement() {

        def replacement(m: Matcher): String = {
          var list = m.group(1)
          var listStart = m.group(3)
          var listType = ""
          listType = if (listStart.matches("[*+-]")) "ul" else "ol"
          list = replaceAll(list, "\n{2,}", "\n\n\n")
          var result = processListItems(list)
          var html = if (listStart.matches("[*+-]")) "<ul>\n" + result + "</ul>\n" else "<ol>\n" + result + "</ol>\n"
          return html
        }
      }
      val matchStartOfLine = Pattern.compile("(?:(?<=\\n\\n)|\\A\\n?)" + wholeList, Pattern.MULTILINE)
      text.replaceAll(matchStartOfLine, replacer)
    }
    text
  }

  private def processListItems(pList: String): String = {
    var list = pList
    listLevel += 1
    list = replaceAll(list, "\\n{2,}\\z", "\n")
    val p = Pattern.compile("(\\n)?" + "^([ \\t]*)([-+*]|\\d+[.])[ ]+" + "((?s:.+?)(\\n{1,2}))" + 
      "(?=\\n*(\\z|\\2([-+\\*]|\\d+[.])[ \\t]+))", Pattern.MULTILINE)
    list = replaceAll(list, p, new Replacement() {

      def replacement(m: Matcher): String = {
        var text = m.group(4)
        var item = new TextEditor(text)
        var leadingLine = m.group(1)
        if (!isEmptyString(leadingLine) || hasParagraphBreak(item)) {
          item = runBlockGamut(item.outdent())
        } else {
          item = doLists(item.outdent())
          item = runSpanGamut(item)
        }
        return "<li>" + item.trim().toString + "</li>\n"
      }
    })
    listLevel -= 1
    list
  }

  private def hasParagraphBreak(item: TextEditor): Boolean = item.toString.indexOf("\n\n") != -1

  private def isEmptyString(leadingLine: String): Boolean = {
    leadingLine == null || leadingLine == ""
  }

  private def doHeaders(markup: TextEditor): TextEditor = {
    markup.replaceAll("^(.*)\n====+$", "<h1>$1</h1>")
    markup.replaceAll("^(.*)\n----+$", "<h2>$1</h2>")
    val p = Pattern.compile("^(#{1,6})\\s*(.*?)\\s*\\1?$", Pattern.MULTILINE)
    markup.replaceAll(p, new Replacement() {

      def replacement(m: Matcher): String = {
        val marker = m.group(1)
        val heading = m.group(2)
        val level = marker.length
        val tag = "h" + level
        return "<" + tag + ">" + heading + "</" + tag + ">\n"
      }
    })
    markup
  }

  private def join(separator: String, strings: Array[String]): String = {
    val length = strings.length
    val buf = new StringBuffer()
    if (length > 0) {
      buf.append(strings(0))
      for (i <- 1 until length) {
        buf.append(separator).append(strings(i))
      }
    }
    buf.toString
  }

  def runSpanGamut(pText: TextEditor): TextEditor = {
	var text = pText
    text = escapeSpecialCharsWithinTagAttributes(text)
    text = doCodeSpans(text)
    text = encodeBackslashEscapes(text)
    doImages(text)
    doAnchors(text)
    doAutoLinks(text)
    text = escapeSpecialCharsWithinTagAttributes(text)
    encodeAmpsAndAngles(text)
    doItalicsAndBold(text)
    text.replaceAll(" {2,}\n", " <br />\n")
    text
  }

  /**
   * escape special characters
   *
   * Within tags -- meaning between < and > -- encode [\ ` * _] so they
   * don't conflict with their use in Markdown for code, italics and strong.
   * We're replacing each such character with its corresponding random string
   * value; this is likely overkill, but it should prevent us from colliding
   * with the escape values by accident.
   *
   * @param text
   * @return
   */
  private def escapeSpecialCharsWithinTagAttributes(text: TextEditor): TextEditor = {
    val tokens = text.tokenizeHTML()
    val newText = new TextEditor("")
    for (token <- tokens) {
      var value = ""
      value = token.getText
      if (token.isTag) {
        value = value.replaceAll("\\\\", CHAR_PROTECTOR.encode("\\"))
        value = value.replaceAll("`", CHAR_PROTECTOR.encode("`"))
        value = value.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
        value = value.replaceAll("_", CHAR_PROTECTOR.encode("_"))
      }
      newText.append(value)
    }
    newText
  }

  private def doImages(text: TextEditor) {
    text.replaceAll("!\\[(.*)\\]\\((.*) \"(.*)\"\\)", "<img src=\"$2\" alt=\"$1\" title=\"$3\" />")
    text.replaceAll("!\\[(.*)\\]\\((.*)\\)", "<img src=\"$2\" alt=\"$1\" />")
  }

  private def doAnchors(markup: TextEditor): TextEditor = {
    val internalLink = Pattern.compile("(" + "\\[(.*?)\\]" + "[ ]?(?:\\n[ ]*)?" + "\\[(.*?)\\]" + 
      ")")
    markup.replaceAll(internalLink, new Replacement() {

      def replacement(m: Matcher): String = {
        var replacementText: String = ""
        val wholeMatch = m.group(1)
        val linkText = m.group(2)
        var id = m.group(3).toLowerCase()
        if (id == null || "" == id) {
          id = linkText.toLowerCase()
        }
        val defn = linkDefinitions.get(id)
        if (defn != null) {
          var url = defn.getUrl
          url = url.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
          url = url.replaceAll("_", CHAR_PROTECTOR.encode("_"))
          var title = defn.getTitle
          var titleTag = ""
          if (title != null && title != "") {
            title = title.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
            title = title.replaceAll("_", CHAR_PROTECTOR.encode("_"))
            titleTag = " title=\"" + title + "\""
          }
          replacementText = "<a href=\"" + url + "\"" + titleTag + ">" + linkText + 
            "</a>"
        } else {
          replacementText = wholeMatch
        }
        return replacementText
      }
    })
    val inlineLink = Pattern.compile("(" + "\\[(.*?)\\]" + "\\(" + "[ \\t]*" + "<?(.*?)>?" + 
      "[ \\t]*" + 
      "(" + 
      "(['\"])" + 
      "(.*?)" + 
      "\\5" + 
      ")?" + 
      "\\)" + 
      ")", Pattern.DOTALL)
    markup.replaceAll(inlineLink, new Replacement() {

      def replacement(m: Matcher): String = {
        val linkText = m.group(2)
        var url = m.group(3)
        var title = m.group(6)
        url = url.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
        url = url.replaceAll("_", CHAR_PROTECTOR.encode("_"))
        val result = new StringBuffer()
        result.append("<a href=\"").append(url).append("\"")
        if (title != null) {
          title = title.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
          title = title.replaceAll("_", CHAR_PROTECTOR.encode("_"))
          title = replaceAll(title, "\"", "&quot;")
          result.append(" title=\"")
          result.append(title)
          result.append("\"")
        }
        result.append(">").append(linkText)
        result.append("</a>")
        return result.toString
      }
    })
    val referenceShortcut = Pattern.compile("(" + "\\[" + "([^\\[\\]]+)" + "\\]" + ")", Pattern.DOTALL)
    markup.replaceAll(referenceShortcut, new Replacement() {

      def replacement(m: Matcher): String = {
        var replacementText: String = ""
        val wholeMatch = m.group(1)
        val linkText = m.group(2)
        var id = m.group(2).toLowerCase()
        id = id.replaceAll("[ ]?\\n", " ")
        val defn = linkDefinitions.get(id.toLowerCase())
        if (defn != null) {
          var url = defn.getUrl
          url = url.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
          url = url.replaceAll("_", CHAR_PROTECTOR.encode("_"))
          var title = defn.getTitle
          var titleTag = ""
          if (title != null && title != "") {
            title = title.replaceAll("\\*", CHAR_PROTECTOR.encode("*"))
            title = title.replaceAll("_", CHAR_PROTECTOR.encode("_"))
            titleTag = " title=\"" + title + "\""
          }
          replacementText = "<a href=\"" + url + "\"" + titleTag + ">" + linkText + 
            "</a>"
        } else {
          replacementText = wholeMatch
        }
        return replacementText
      }
    })
    markup
  }

  private def doItalicsAndBold(markup: TextEditor): TextEditor = {
    markup.replaceAll("(\\*\\*|__)(?=\\S)(.+?[*_]*)(?<=\\S)\\1", "<strong>$2</strong>")
    markup.replaceAll("(\\*|_)(?=\\S)(.+?)(?<=\\S)\\1", "<em>$2</em>")
    markup
  }

  private def encodeAmpsAndAngles(markup: TextEditor): TextEditor = {
    markup.replaceAll("&(?!#?[xX]?(?:[0-9a-fA-F]+|\\w+);)", "&amp;")
    markup.replaceAll("<(?![a-z/?\\$!])", "&lt;")
    markup
  }

  private def doCodeSpans(markup: TextEditor): TextEditor = {
    markup.replaceAll(Pattern.compile("(?<!\\\\)(`+)(.+?)(?<!`)\\1(?!`)"), new Replacement() {

      def replacement(m: Matcher): String = {
        val code = m.group(2)
        val subEditor = new TextEditor(code)
        subEditor.deleteAll("^[ \\t]+").deleteAll("[ \\t]+$")
        encodeCode(subEditor)
        "<code>" + subEditor.toString + "</code>"
      }
    })
  }

  private def deleteAll(text: String, regex: String): String = replaceAll(text, regex, "")

  private def replaceAll(text: String, regex: String, replacement: String): String = {
    val ed = new TextEditor(text)
    ed.replaceAll(regex, replacement)
    ed.toString
  }

  private def replaceAll(markup: String, pattern: Pattern, replacement: Replacement): String = {
    val ed = new TextEditor(markup)
    ed.replaceAll(pattern, replacement)
    ed.toString
  }

  override def toString(): String = {
    "Markdown Processor for Java 0.4.0 (compatible with Markdown 1.0.2b2)"
  }
}
