package org.wymiwyg.gradino

import javax.ws.rs._
import javax.ws.rs.core._
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.{UriRef, BNode}
import org.apache.clerezza.rdf.ontologies.{RDF, DC}
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.rdf.core.sparql.QueryEngine
import org.apache.clerezza.rdf.core.sparql.QueryParser
import org.apache.clerezza.rdf.core.sparql.ResultSet
import org.apache.clerezza.rdf.core.sparql.query.Query
import org.apache.clerezza.rdf.scala.utils.EzMGraph
import org.apache.clerezza.rdf.scala.utils.RichGraphNode

/**
 * shows the latest items of the blog
 */
@SupportedTypes(types = 
Array(Ontology.LatestItemsPage_String))
class LatestItemsTypeHandler(context: BundleContext) {

	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	@GET def get(@Context uriInfo: UriInfo) = {
		val uriString = uriInfo.getAbsolutePath().toString()
		val uri = new UriRef(uriString)
		
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val resultMGraph = new UnionMGraph(new SimpleMGraph(), cg);
		val ezResultMGraph = new EzMGraph(resultMGraph)
		import ezResultMGraph._
		val graphNode: GraphNode =uri
		uri a RDF.List
		//graphNode.addProperty(RDF.`type`, Ontology.LatestItemsPage)
		import collection.JavaConversions._
		val list = uri.asList
		val allItemsService = $[LatestItemsService]
		val allItems = allItemsService.getItems
		val pageSubjects = uri/DC.subject
		val items = if ((uri/DC.subject).size > 0) {
			allItems.filter(item => (item._2/DC.subject).exists(itemSubject => pageSubjects.exists(_ == itemSubject)))
		} else {
			allItems
		}
		//allItems.fi
		val first10 = items.splitAt(10)._1
		for (item <- first10) {
			list.add(item._2)
		}
		graphNode;
	}
}
