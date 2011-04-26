package org.farewellutopia.blog

import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions.asJavaDictionary
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.ontologies.RDF

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends BundleActivator {

	var helloWorldRegistration: ServiceRegistration = null
	var itemRenderletRegistration: ServiceRegistration = null
	var resourceRenderletRegistration: ServiceRegistration = null
	/**
	 * called when the bundle is started, this method initializes the provided service
	 */
	def start(context: BundleContext) {
		val servicesDsl = new ServicesDsl(context)
		import servicesDsl._
		println("activating...")
		//adding some triples to the content graph
		val cgp = $[ContentGraphProvider]
		val cg = cgp.getContentGraph
		cg.add(new TripleImpl(new UriRef("http://localhost:8080/"), RDF.`type`, Ontology.LatestItemsPage))

		val args = scala.collection.mutable.Map("org.apache.clerezza.platform.typehandler" -> true)
		helloWorldRegistration = context.registerService(classOf[Object].getName,
												  new LatestItemsTypeHandler(context), args)
		val renderlet = new ItemRenderlet
		itemRenderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  renderlet, null)
		resourceRenderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  new ResourceRenderlet, null)
		println("enjoy it!")
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		helloWorldRegistration.unregister()
		itemRenderletRegistration.unregister()
		resourceRenderletRegistration.unregister()
		println("bye")
	}

}
