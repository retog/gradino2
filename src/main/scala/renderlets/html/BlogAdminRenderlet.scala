package org.wymiwyg.gradino.renderlets.html

import org.wymiwyg.gradino.Ontology
import scala.xml.XML
import javax.ws.rs.core.MediaType
import java.util.Date
import java.text.DateFormat
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies._
import org.xml.sax.SAXParseException


/**
 * A Renderlet for rss:items
 */
class BlogAdminRenderlet extends SRenderlet {

	val getRdfType = Ontology.BlogAdminPage

	override def getModePattern = "naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				val baseUri = arguments.requestProperties.getUriInfo.getBaseUri.toString
				<div>
					<h1>Blog Administration</h1>
					At the following URIs the most recent posts can be retrieved:
					{
						for (lip <- res!!) yield {
							<div>
								<form action="/gradino/removeLip" method="post">
									<span>{lip}</span><input type="hidden" name="lip" value={lip*} />
									<input type="submit" value="remove"/>
								</form>
							</div>
						}
					}
					<form action="/gradino/addLip" method="post">
						Add Uri:
						<input type="text" name="lip" value={baseUri} />
						<input type="submit" value="Add"/>
					</form>
					<p>
						<a href={baseUri+"gradino/newpost?mode=tiny"}>Create new post with tinyMCE wysiwyg editor</a><br/>
						<a href={baseUri+"gradino/newpost?mode=markDown"}>Create new post writing MarkDown enriched text</a><br/>
					</p>
				</div>
			}
		}
	}

}