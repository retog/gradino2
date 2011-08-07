package org.wymiwyg.gradino

import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions.asJavaDictionary
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.ontologies.RDF
import renderlets.html._
import renderlets._

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends BundleActivator with ActivationHelper {


	registerRootResource(new Gradino(context))
	registerTypeHandler(new LatestItemsTypeHandler(context))
	//registerRenderlet(new ResourceRenderlet)
	registerRenderlet(new BlogAdminRenderlet)
	registerRenderlet(new HeadedItemRenderlet)
	registerRenderlet(new ItemNakedRenderlet)
	registerRenderlet(new atom.ItemRenderlet)
	registerRenderlet(new atom.RdfListRenderlet)
	registerRenderlet(new atom.TitledContentRenderlet)
	registerRenderlet(new ItemTinyMCERenderlet)
	registerRenderlet(new ItemZariaRenderlet)
	registerRenderlet(new ItemMarkDownRenderlet)
	registerService(new LatestItemsService(context), classOf[LatestItemsService])
	registerService(new TitledContentDateAdder(context), classOf[TitledContentDateAdder])

}
