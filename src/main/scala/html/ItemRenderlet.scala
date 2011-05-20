package com.farewellutopia.blog

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
class ItemRenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	override def getModePattern = "naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				val selfLink = res.getNode match {
					case u: UriRef => u.getUnicodeString
					case b: BNode => "/blog/newpost"
				}

				val editLink =  selfLink+"?mode=form"
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

				val xmlContent = try {
					XML.loadString("<root>"+(res/Ontology.content*)+"</root>").child
				} catch {
					case ex: SAXParseException => <div><strong>The following literal could not be parsed as XHTML:<br/></strong> {res/Ontology.content*}</div>
				}
				<div class="hentry"><h2 class="entry-title"><a href={selfLink}>{res/Ontology.title*}</a>
					<a href={editLink}><img src="/icons/edit.png" /></a></h2>
                  {xmlContent}
                  <p class="author">{res/FOAF.maker/FOAF.name*}</p>
                  <p class="published">pub:{
										try {
											dateFormat.format((res/DC.date).as[Date]) //assumes its a typed literal
										} catch {
											case e: ClassCastException => res/DC.date*
										}
									}</p>
                  <p class="tag">{tags}</p>
                  <!-- <p><a href={ related }>Related</a></p>
                  <p><a href={ comment }>Comments</a></p> -->
               </div>
			}
		}
	}

}