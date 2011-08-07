package org.wymiwyg.gradino.renderlets.atom

import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import javax.ws.rs.core.MediaType

/**
 * A Renderlet for rdf:ListS
 */
class RdfListRenderlet extends SRenderlet {

	val getRdfType = RDF.List

	override val getMediaType = MediaType.APPLICATION_ATOM_XML_TYPE

	override def getModePattern = "atom"
	
	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				<feed xmlns="http://www.w3.org/2005/Atom">
					<id>{res*}</id>
					{for (entry <- res!!) yield render(entry, "atom")
					}
				</feed>
			}
		}
	}

}
