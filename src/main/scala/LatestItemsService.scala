package org.farewellutopia.blog

import javax.ws.rs._
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import java.util.Date
import org.apache.clerezza.rdf.ontologies.DC
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import util.Sorting

/**
 * a service returning available items in inverse cronological order (latest
 * first) by their DC.date property
 */
class LatestItemsService(context: BundleContext) {
	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	private var items = List[(Date, UriRef)]();
	{
		def asDate(l: Literal): Date = {
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
				val date = asDate(stmt.getObject.asInstanceOf[Literal]);
				items ::= ((date, subj.asInstanceOf[UriRef]));
			}
		}
		println("collected")
		val itemsArray = items.toArray
		Sorting.quickSort(itemsArray)(Ordering[Long].on[(Date, UriRef)](_._1.getTime).reverse);
		items = itemsArray.toList
		println("sorted "+items.size)
		println("first "+items.head)
	}
}
