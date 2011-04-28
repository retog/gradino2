package org.farewellutopia.blog

import javax.ws.rs._
import org.apache.clerezza.osgi.services.ServicesDsl
import org.osgi.framework.BundleContext
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.jena.facade.JenaGraph
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.util.Date
import java.text.SimpleDateFormat
import org.apache.clerezza.rdf.scala.utils.Preamble
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.{BNode, UriRef}
import org.apache.clerezza.rdf.utils.{UnionMGraph, GraphNode}
import org.apache.clerezza.rdf.core.impl.{PlainLiteralImpl, SimpleMGraph}
import org.apache.clerezza.rdf.ontologies.{DC, RDF}
import java.security.AccessController
import org.apache.clerezza.rdf.core.access.security.TcPermission

@Path("blog")
class Blog(context: BundleContext) {

	println("constr blog w "+context)
	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	//TODO get from service
	private val baseUri = "http://localhost:8080"

	@GET
	@Path("newpost")
	def newPost() = {
		//There's mno point showing this page to users that cannot write to the content graph
		AccessController.checkPermission(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READWRITE))
		val resultMGraph = new SimpleMGraph();
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val graphNode = new GraphNode(new BNode(), new UnionMGraph(cg, resultMGraph))
		graphNode.addProperty(RDF.`type`, Ontology.Item)
		graphNode.addPropertyValue(DC.date, new Date)
		graphNode.addProperty(Ontology.title, new PlainLiteralImpl("a blank item"))
		graphNode
	}


	@POST
	@Path("addpost")
	@Produces(Array("text/html"))
	def handle(@FormParam("title") title: String,
				@FormParam("content") content: String, @FormParam("makerName") makerName: String,
				@FormParam("tags") tags : String, @FormParam("date") date : String) = {
		insertItem(title, content, makerName, tags, date)
	}

	private def insertItem(title: String, content: String, makerName : String, tags : String,
												 existingDate : String) = {
		val date =
			if( existingDate=="" || existingDate == null ){
				getDate()
			} else existingDate

		val uri = buildUri(title)

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
		jenaModel.add(item, jenaModel.createProperty("http://planetrdf.com/ns/content"), content)
		jenaModel.add(item, jenaModel.createProperty("http://purl.org/dc/elements/1.1/date"), date)
		jenaModel.add(item, jenaModel.createProperty("http://xmlns.com/foaf/0.1/maker"), maker)
		jenaModel.add(maker, jenaModel.createProperty("http://xmlns.com/foaf/0.1/name"), makerName)

		//doTags(item, tags)
		jenaModel.close
		System.out.println("item inserted/modified: "+item)
	}
	private def getDate() : String = {
        val now = new Date();
        val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+01:00");
        return sdf.format( now );
	}

	private def buildUri(title : String) : String = {
        val now = new Date();
        val sdf = new SimpleDateFormat("yyyy/MM/dd/");

		var uri = baseUri+"/"+ sdf.format( now )
		var stub = title.replaceAll(" ","-")
		uri = uri+stub
		// System.out.println("URI for POSTed item = "+uri)
		return uri
	}
}