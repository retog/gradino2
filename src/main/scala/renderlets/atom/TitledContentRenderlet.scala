package org.wymiwyg.gradino.renderlets.atom

import org.wymiwyg.gradino.Ontology
import org.wymiwyg.gradino.renderlets.common._
import _root_.scala.xml.XML
import javax.ws.rs.core.MediaType
import java.util.Date
import java.text.DateFormat
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.core._
import com.petebevin.markdown.MarkdownProcessor
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies._
import org.xml.sax.SAXParseException
import javax.ws.rs.core.MediaType


/**
 * A Renderlet for titled contents
 */
class TitledContentRenderlet extends SRenderlet {

	val getRdfType = DISCOBITS.TitledContent

	override def getModePattern = "atom"

	override val getMediaType = MediaType.APPLICATION_ATOM_XML_TYPE

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
  <entry xmlns="http://www.w3.org/2005/Atom">

    <title>unknown</title>
    <link rel="alternate" type="text/html" href="http://www.jenitennison.com/blog/node/162" />
    <id>{res*}</id>
    <published></published>
    <updated></updated>
    <author>
      <name></name>
    </author>
    <category term="microdata" />
    <category term="rdf" />
    <category term="rdfa" />
    <summary type="html"></summary>
    <content type="html">unknown</content>
  </entry>
			}
		}
	}

}