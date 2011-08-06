package org.wymiwyg.gradino.renderlets.common

import org.apache.clerezza._
import rdf.core._
import rdf.ontologies._
import rdf.scala.utils._
import Preamble._
import org.wymiwyg.gradino._

class ItemDecorator(res: RichGraphNode) {

	import org.xml.sax.SAXParseException
	import com.petebevin.markdown.MarkdownProcessor
	val xmlContent = if (((res/Ontology.content).size > 0) &&
			(((res/Ontology.content)(0)).getNode.asInstanceOf[TypedLiteral].getDataType == RDF.XMLLiteral)) {
		try {
			xml.XML.loadString("<root>"+(res/Ontology.content*)+"</root>").child
		} catch {
			case ex: SAXParseException => <div><strong>The following literal could not be parsed as XHTML:<br/></strong> {res/Ontology.content*}</div>
		}
	} else {
		try {
			xml.XML.loadString("<root>"+new MarkdownProcessor().markdown(res/Ontology.content*)+"</root>").child
		} catch {
			case ex: SAXParseException => <div><strong>The following mardown processor output could not be parsed as  XHTML:<br/></strong> {res/Ontology.content*}</div>
		}
	}

}

object ItemDecorator {

	def apply(res: RichGraphNode) = new ItemDecorator(res)
}
