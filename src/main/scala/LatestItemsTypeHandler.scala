package com.farewellutopia.blog

import javax.ws.rs._
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

/**
 * shows the latest items of the blog
 */
@SupportedTypes(types = Array(Ontology.LatestItemsPage_String),
	prioritize = true)
class LatestItemsTypeHandler(context: BundleContext) {

	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	@GET def get() = {
		val resultMGraph = new SimpleMGraph();
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val graphNode = new GraphNode(new BNode(), new UnionMGraph(resultMGraph, cg));
		import collection.JavaConversions._
		val list = graphNode.asList
		val allItems = $[LatestItemsService]
		val first10 = allItems.getItems.splitAt(10)._1
		for (item <- first10) {
			list.add(item._2)
		}
		graphNode;
	}
}
