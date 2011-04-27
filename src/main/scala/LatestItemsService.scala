package org.farewellutopia.blog

import javax.ws.rs._
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.rdf.core.sparql.QueryEngine
import org.apache.clerezza.rdf.core.sparql.QueryParser
import org.apache.clerezza.rdf.core.sparql.ResultSet
import org.apache.clerezza.rdf.core.sparql.query.Query
import java.util.Date
import collection.SortedMap
import org.apache.clerezza.rdf.ontologies.{DC, RDF}
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat

/**
 * shows the latest items of the blog
 */
class LatestItemsService(context: BundleContext) {
	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	private var items = SortedMap[Date, UriRef]()(Ordering[Long].on[Date](_.getTime).reverse);
	{
		def asDate(l: Literal) = {
			try {
				l match {
					case l: TypedLiteral => LiteralFactory.getInstance.createObject(classOf[Date], l)
					case o =>  W3CDateFormat.instance.parse(o.getLexicalForm)
				}
			} catch {
				case e => println("unparseable date: "+l.getLexicalForm); new Date(0)
			}
		}
		val resultMGraph = new SimpleMGraph();
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		val stmts = cg.filter(null, DC.date, null)
		while (stmts.hasNext) {
			val stmt = stmts.next
			val subj = stmt.getSubject
			if (subj.isInstanceOf[UriRef]) {
				val date = asDate(stmt.getObject.asInstanceOf[Literal])
				items += (date -> subj.asInstanceOf[UriRef])
			}
		}
		println("sorted "+items.size)
	}
}
