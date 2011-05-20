package com.farewellutopia.blog

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
class Activator extends BundleActivator with ActivationHelper {


	registerRootResource(new Blog(context))
	registerTypeHandler(new LatestItemsTypeHandler(context))
	registerRenderlet(new ResourceRenderlet)
	registerRenderlet(new ItemRenderlet)
	registerRenderlet(new ItemFormRenderlet)
	registerRenderlet(new ItemZariaRenderlet)
	registerService(new LatestItemsService(context), classOf[LatestItemsService])

	/**
	 * called when the bundle is started, this method initializes the provided service
	 */
	override def start(context: BundleContext) {
		super.start(context)
		val servicesDsl = new ServicesDsl(context)
		import servicesDsl._
		println("activating...")
		//adding some triples to the content graph
		doWith[ContentGraphProvider]{cgp =>
			val cg = cgp.getContentGraph
			cg.add(new TripleImpl(new UriRef("http://localhost:8080/"), RDF.`type`, Ontology.LatestItemsPage))
			cg.add(new TripleImpl(new UriRef("https://localhost:8443/"), RDF.`type`, Ontology.LatestItemsPage))
		}
		println("enjoy it!")
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	override def stop(context: BundleContext) {
		super.stop(context)
		println("bye")
	}

}
