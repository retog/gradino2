package org.farewellutopia.blog

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
	private var query: Query = null
	doWith[QueryParser] {
		qp => query = qp.parse("""prefix dc: <http://purl.org/dc/elements/1.1/>
              prefix foaf:  <http://xmlns.com/foaf/0.1/>
              prefix rss:  <http://purl.org/rss/1.0/>
             prefix content:  <http://purl.org/rss/1.0/modules/content/>
              prefix planet: <http://planetrdf.com/ns/>

              SELECT ?item  WHERE {

              	?item a rss:item ;
              		rss:title ?title ;
              		planet:content ?content ;
              		dc:date ?date ;
                    foaf:maker ?maker .
                    ?maker foaf:name ?makerName .
              }
              ORDER BY DESC(?date)
              LIMIT 20""")
	}
	@GET def get() = {
		val resultMGraph = new SimpleMGraph();
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val graphNode = new GraphNode(new BNode(), new UnionMGraph(cg, resultMGraph));
		//graphNode.addProperty(RDF.`type` , Ontology.LatestItemsPage);
		val qe = $[QueryEngine]
		val qResult = qe.execute(null, cg, query).asInstanceOf[ResultSet]
		import collection.JavaConversions._
		for (sm <- qResult) {
			println(sm.get("item"))
		}
		val list = graphNode.asList
		list.add(new UriRef("http://foo"))
		graphNode.addPropertyValue(DC.description,"Hello world of "+cgp.getContentGraph.size);
		graphNode;

	}
}
