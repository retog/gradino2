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
import rdf.ontologies.{DC, RDF}
import java.net.URI
import java.security.AccessController
import rdf.core.access.security.TcPermission

@Path("gradino")
class Gradino(context: BundleContext) extends Logging {

	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	private def baseUri = {
		import platform.config.PlatformConfig
		val pc = $[PlatformConfig]
		if (pc != null) {
			pc.getDefaultBaseUri.getUnicodeString
		} else {
			logger.warn("Couldn't access PlatformConfig")
			"http://localhost:8080/"
		}

	}

	@GET
	def default() = {
		val result = new RichGraphNode(new BNode, new SimpleMGraph)
		result a Ontology.BlogAdminPage
		val resultList = result.asList
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val ezCg = new EzMGraph(cg)
		import ezCg._
		for (ltp <- Ontology.LatestItemsPage/-RDF.`type`) {
			resultList.add(ltp.getNode)
		}
		result
	}

	@POST
	@Path("removeLip")
	def removeLip(@FormParam("lip") lip: UriRef, @Context baseUri: UriInfo) = {
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val ezCg = new EzMGraph(cg)
		import ezCg._
		lip.deleteProperty(RDF.`type`, Ontology.LatestItemsPage)
		RedirectUtil.createSeeOtherResponse("/gradino", baseUri)
	}

	@POST
	@Path("addLip")
	def addLip(@FormParam("lip") lip: UriRef, @Context baseUri: UriInfo) = {
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val ezCg = new EzMGraph(cg)
		import ezCg._
		lip a Ontology.LatestItemsPage
		RedirectUtil.createSeeOtherResponse("/gradino", baseUri)
	}

	@GET
	@Path("newpost")
	def newPost() = {
		//There's mno point showing this page to users that cannot write to the content graph
		AccessController.checkPermission(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READWRITE))
		val resultMGraph = new SimpleMGraph();
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val graphNode = new GraphNode(new BNode(), new UnionMGraph(resultMGraph, cg))
		graphNode.addProperty(RDF.`type`, Ontology.Item)
		graphNode.addPropertyValue(DC.date, new Date)
		graphNode.addProperty(Ontology.title, new PlainLiteralImpl("a blank item"))
		graphNode
	}


	@POST
	@Path("addpost")
	@Produces(Array("text/html"))
	def handle(@FormParam("title") title: String,
				@FormParam("content") content: String,
				@FormParam("contentMarkDown") contentMarkDown: String,
				@FormParam("makerName") makerName: String,
				@FormParam("uri") uriString: String,
			   @FormParam("tags") tags : String, @FormParam("date") date : String) = {
		val item: UriRef = insertItem(title, content, contentMarkDown, makerName, uriString, tags, date)
		var responseBuilder: ResponseBuilder = Response.status(Response.Status.SEE_OTHER);
		responseBuilder = responseBuilder.entity("item saved");
		responseBuilder = responseBuilder.location(new URI(item.getUnicodeString));
		responseBuilder.build
	}

	private def insertItem(title: String, content: String, contentMarkDown: String,
						   makerName : String,
						   uriString: String, tags : String,
												 existingDate : String) = {
		val date =
			if( existingDate=="" || existingDate == null ){
				getDate()
			} else existingDate

		val uri = if (uriString == "<new>") {
				buildUri(title)
			} else {
				uriString
			}

		val tcm = $[TcManager]
		val contentGraph = tcm.getMGraph(Constants.CONTENT_GRAPH_URI);
		{
			//a small example on to do thing without jena api
			val p = new Preamble(contentGraph)
			val g = new GraphNode(new UriRef(uri), contentGraph)
			g.deleteNodeContext
		}


		val jenaGraph = new JenaGraph(contentGraph)
		val jenaModel = ModelFactory.createModelForGraph(jenaGraph)
        // System.out.println("inserting item")
		// ?title ?content ?date ?makerName ?related ?comment

		var item = jenaModel.createResource(uri) // need to handle replace flag

		//jenaModel.removeAll(item, null, null)

		var itemType = jenaModel.createResource("http://purl.org/rss/1.0/item")
		var maker = jenaModel.createResource()

		jenaModel.add(item, jenaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), itemType)
		jenaModel.add(item, jenaModel.createProperty("http://purl.org/rss/1.0/title"), title)
		//jenaModel.add(item, jenaModel.createProperty("http://planetrdf.com/ns/content"), content)
		jenaModel.add(item, jenaModel.createProperty("http://purl.org/dc/elements/1.1/date"), jenaModel.createTypedLiteral(date, XSD.dateTime.getURI))
		jenaModel.add(item, jenaModel.createProperty("http://xmlns.com/foaf/0.1/maker"), maker)
		jenaModel.add(maker, jenaModel.createProperty("http://xmlns.com/foaf/0.1/name"), makerName)

		//doTags(item, tags)
		jenaModel.close
		System.out.println("item inserted/modified: "+item)
		val itemRes = new UriRef(uri)
		val contentLit = if (content != null) {
			new TypedLiteralImpl(content, RDF.XMLLiteral)
		} else {
			LiteralFactory.getInstance.createTypedLiteral(contentMarkDown)
		}
		contentGraph.add(new TripleImpl(itemRes, 
			new UriRef("http://planetrdf.com/ns/content"), contentLit))
		itemRes
	}
	private def getDate() : String = {
        val now = new Date();
        val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+01:00");
        return sdf.format( now );
	}

	private def buildUri(title : String) : String = {
        val now = new Date();
        val sdf = new SimpleDateFormat("yyyy/MM/dd/");

		var uri = baseUri+ sdf.format( now )
		var stub = title.replaceAll(" ","-")
		uri = uri+stub
		// System.out.println("URI for POSTed item = "+uri)
		return uri
	}
}