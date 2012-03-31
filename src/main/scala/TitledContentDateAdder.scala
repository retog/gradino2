package org.wymiwyg.gradino

import javax.ws.rs._
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.scala.utils.EzMGraph
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import java.util.Date
import org.apache.clerezza.rdf.ontologies.{DC, RDF, DISCOBITS}
import org.apache.clerezza.rdf.core._
import event._
import impl.util.W3CDateFormat
import util.Sorting
import _root_.scala.actors.Actor.actor


/**
 * adds a date property to newly created TitledContentS if these aren't hold
 * themself in an entry'
 */
class TitledContentDateAdder(context: BundleContext) extends GraphListener {
	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._
	
	private val tcm: TcManager = $[TcManager]
	private val cg = tcm.getMGraph(Constants.CONTENT_GRAPH_URI)

	cg.addGraphListener(this, new FilterTriple(null, RDF.`type`, DISCOBITS.TitledContent), 1000)

	def graphChanged(events: java.util.List[GraphEvent]) {
		import collection.JavaConversions._
		for (e <- events) {
			println("processing "+e)
			e match {
				case e: AddEvent => {
					delayedAddMissingDate(e.getTriple.getSubject)
				}
			}
		}
	}

	private def delayedAddMissingDate(resource: Resource) {
		actor {
			Thread.sleep(1000)
			new EzMGraph(cg) {
				if ((resource/DC.date).size == 0) {
					resource -- DC.date --> new Date()
				}
			}
		}
	}
}
