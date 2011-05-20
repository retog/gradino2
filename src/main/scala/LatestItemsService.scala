package com.farewellutopia.blog

import javax.ws.rs._
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import java.util.Date
import org.apache.clerezza.rdf.ontologies.DC
import org.apache.clerezza.rdf.core._
import event._
import impl.util.W3CDateFormat
import util.Sorting

/**
 * a service returning available items in inverse cronological order (latest
 * first) by their DC.date property
 */
class LatestItemsService(context: BundleContext) extends GraphListener {
	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	def getItems = items

	private var items = List[(Date, UriRef)]();
	private def prependItemFromTriple(t: Triple) {
		val subj = t.getSubject
		if (subj.isInstanceOf[UriRef]) {
			val date = asDate(t.getObject.asInstanceOf[Literal]);
			items ::= ((date, subj.asInstanceOf[UriRef]));
		}
	}
	
	def asDate(l: Literal): Date = {
		try {
			l match {
				case l: TypedLiteral => LiteralFactory.getInstance.createObject(classOf[Date], l)
				case o =>  W3CDateFormat.instance.parse(o.getLexicalForm)
			}
		} catch {
			case e => println("unparseable date: "+l.getLexicalForm); new Date(0)
		}
	};
	{
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		val cg = cgp.getContentGraph;
		val stmts = cg.filter(null, DC.date, null)
		while (stmts.hasNext) {
			val stmt = stmts.next
			prependItemFromTriple(stmt);
		}
		println("collected")
		val itemsArray = items.toArray
		Sorting.quickSort(itemsArray)(Ordering[Long].on[(Date, UriRef)](_._1.getTime).reverse);
		items = itemsArray.toList
		println("sorted "+items.size)
	}
	private val tcm: TcManager = $[TcManager]
	private val cg = tcm.getMGraph(Constants.CONTENT_GRAPH_URI)
	cg.addGraphListener(this, new FilterTriple(null, DC.date, null), 1000)

	def graphChanged(events: java.util.List[GraphEvent]) {
		val itemsArray = items.toArray
		import collection.JavaConversions._
		for (e <- events) {
			println("processing "+e)
			e match {
				case e: RemoveEvent => items = items.filterNot(_._2 == e.getTriple.getSubject)
				case e: AddEvent => {
					prependItemFromTriple(e.getTriple)
					//we just assume the just added item has the most recent date, or we should resort here
				}
			}
		}
	}
}
