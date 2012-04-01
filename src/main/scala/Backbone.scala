package org.wymiwyg.gradino

import javax.ws.rs._
import com.hp.hpl.jena.vocabulary.XSD
import core.Context
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.rdf.scala.utils.EzMGraph
import org.apache.clerezza.rdf.scala.utils.RichGraphNode
import org.osgi.framework.BundleContext
import org.apache.clerezza.jaxrs.utils.RedirectUtil
import org.apache.clerezza.rdf.core.LiteralFactory
import org.apache.clerezza.rdf.core.TypedLiteral
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl
import org.apache.clerezza.rdf.jena.facade.JenaGraph
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.util.Date
import java.text.SimpleDateFormat
import org.slf4j.scala.Logging
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.UriInfo
import org.apache.clerezza._
import rdf.scala.utils.Preamble
import platform.graphprovider.content.ContentGraphProvider
import rdf.core.{BNode, UriRef}
import rdf.utils.{UnionMGraph, GraphNode}
import rdf.core.impl.{PlainLiteralImpl, SimpleMGraph}
import rdf.ontologies.{DC, DCTERMS, RDF}
import java.net.URI
import java.security.AccessController
import rdf.core.access.security.TcPermission
import org.json.simple._


@Path("gradino/backbone")
class Backbone(context: BundleContext) extends Logging {

  private val servicesDsl = new ServicesDsl(context)
  import servicesDsl._

  @GET
  def update() = {
	"got get"
  }
  
  @PUT
  @Produces(Array("application/rdf+json"))
  def update(json: String,@Context uriInfo : UriInfo) = {
	val cgp: ContentGraphProvider = $[ContentGraphProvider]
	val cg = cgp.getContentGraph
	val newItem = parse(json)
	newItem.addPropertyValue(DC.date, new java.util.Date)
	val itemUri = if (newItem.getNode.asInstanceOf[UriRef].getUnicodeString().equals("urn:x-magic:new")) {
	  import Preamble._
	  val title = (newItem/DCTERMS.title*);
	  val uri = new UriRef((new UriCreation(context)).buildUri(title))
	  newItem.replaceWith(uri)
	  uri
	} else {
	  newItem.getNode
	}
	val existingItem = new GraphNode(itemUri, cg)
	existingItem.deleteNodeContext()
	cg.addAll(newItem.getGraph())
	//redirect only ajay, not browser:
	/*if (newItem.getNode != itemUri) {
	  RedirectUtil.createSeeOtherResponse(itemUri.asInstanceOf[UriRef].getUnicodeString(), uriInfo)
	} else {*/
		newItem.getGraph()
	//}
  }
        
  /**
   * Obviously this should use some json-ld parser
   * 
   * parses something like: {"@subject":"<http://example.net/blog/news_item>",
   * "@type":["<http://www.w3.org/2002/07/owl#Thing>","<http://rdfs.org/sioc/ns#Post>"],
   * "<http://purl.org/dc/terms/title>":"Hello Danny<br>",
   * "<http://rdfs.org/sioc/ns#content>":"Saving an <i>article</i> with create.js",
   * "<http://purl.org/dc/terms/partOf>":"<http://example.net/blog/>"}
   */
  private def parse(jsonString: String): GraphNode = {
	val jsonObject = JSONValue.parse(jsonString);
	jsonObject match {
	  case j : JSONObject => parse(j);
	  case _ => throw new RuntimeException("unsupported")
	}
          
  }
        
  private def parse(jsonObject: JSONObject) = {
	val subject = jsonObject.get("@subject").asInstanceOf[String]
	val item = if (subject == null) new BNode else parseUriRef(subject)
	println(item)
	val result = new GraphNode(item, new SimpleMGraph)
	addTypes(jsonObject, result)
	addProperties(jsonObject, result)
	result
  }
        
  private def addTypes(jsonObject: JSONObject, result: GraphNode) {
	val typeValue = jsonObject.get("@type")
	typeValue match {
	  case s: String => result.addProperty(RDF.`type`, parseUriRef(s))
	  case a: JSONArray => for (i <- 0 to (a.size - 1)) result.addProperty(RDF.`type`, parseUriRef(a.get(i).asInstanceOf[String]))
	}
  }
  
  private def addProperties(jsonObject: JSONObject, result: GraphNode) {
	import _root_.scala.collection.JavaConversions._
	val keys = jsonObject.keySet
	for (key <- keys; if !key.toString.startsWith("@")) {
	  println(key)
	  println(jsonObject.get(key))
	  addProperty(key.asInstanceOf[String], jsonObject.get(key).asInstanceOf[AnyRef], result)
	}
  }
  
  private def addProperty(key: String, value: Object, result: GraphNode) {
	addProperty(parseUriRef(key), value, result)
  }
  
  private def addProperty(property: UriRef, value: Object, result: GraphNode) {
	value match {
	  case a: JSONArray => for (i <- 0 to (a.size - 1)) addProperty(property, 
																	a.get(i).asInstanceOf[Object], result)
	  case s: Object => result.addPropertyValue(property, s)
	}
  }
  
  private def parseUriRef(s: String) = {
	new UriRef(s.substring(0, s.length-1).substring(1))
  }
}