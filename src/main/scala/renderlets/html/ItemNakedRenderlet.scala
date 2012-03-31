package org.wymiwyg.gradino.renderlets.html

import org.wymiwyg.gradino.Ontology
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
import org.wymiwyg.gradino.renderlets.common._



/**
 * A Renderlet for rss:items
 */
class ItemNakedRenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	override def getModePattern = "naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				val selfLink = res.getNode match {
					case u: UriRef => u.getUnicodeString
					case b: BNode => "/gradino/newpost"
				}

				lazy val dateFormat = {
					val acceptable = requestHeaders.getAcceptableLanguages
					if (acceptable.size > 0) {
						DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, acceptable.get(0))
					} else {
						new W3CDateFormat
					}
				}
				//resultDocModifier.addStyleSheet("/styles/hello-world/style.css")
				def tags: String = "tags: "+(for (tag <- res/Ontology.taggedWith) yield {
					tag*
				}).mkString(" ")

				
				<div class="hentry"><h2 class="entry-title"><a href={selfLink}>{res/Ontology.title*}</a>
					<a href={selfLink+"?mode=tiny"}><img src="/icons/edit.png" /></a>
					<a href={selfLink+"?mode=markDown"}><img src="/icons/markdown.png" /></a></h2>
					{ifx (((res/FOAF.maker/FOAF.name).size > 0) && ((res/FOAF.maker/FOAF.name*) != "")) {
						<p class="author">by {res/FOAF.maker/FOAF.name*}</p>}
					}
                  {ItemDecorator(res).xmlContent}
					<p class="published">
					{
						try {
							dateFormat.format((res/DC.date).as[Date]) //assumes its a typed literal
						} catch {
							case e: ClassCastException => "pub: "+ (res/DC.date*)
						}
					}
					</p>
                  <!-- <p class="tag">{tags}</p> -->
                  <!-- <p><a href={ related }>Related</a></p>
                  <p><a href={ comment }>Comments</a></p> -->
               </div>
			}
		}
	}

}