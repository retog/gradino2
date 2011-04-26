package org.farewellutopia.blog

import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import scala.collection.JavaConversions.asJavaDictionary
import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

trait ActivationHelper extends BundleActivator {

	protected var context: BundleContext= null

	protected def registerRootResource(rootResource: =>Object) {
		pendingRootResources ::= (() => rootResource)
	}

	protected def registerRenderlet(renderlet: =>TypeRenderlet) {
		pendingRenderlets ::= (() => renderlet)
	}

	protected def registerTypeHandler(typeHandler: => Object) {
		 pendingTypeHandler ::= (() => typeHandler)
	}

	def start(context: BundleContext) {
		this.context = context
		val servicesDsl = new ServicesDsl(context)
		import servicesDsl._
		println("helping!")
		registeredServices = Nil
		for (rr <- pendingRootResources) {
			val args = scala.collection.mutable.Map("javax.ws.rs" -> true)
			registeredServices ::= context.registerService(classOf[Object].getName,
												 rr(), args)
		}
		pendingRootResources = Nil
		for (rr <- pendingTypeHandler) {
			val args = scala.collection.mutable.Map("org.apache.clerezza.platform.typehandler" -> true)
			registeredServices ::= context.registerService(classOf[Object].getName,
												  rr(), args)
		}
		pendingTypeHandler = Nil
		for (rr <- pendingRenderlets) {
			registeredServices ::= context.registerService(classOf[TypeRenderlet].getName,
												  rr(), null)
		}
		pendingRenderlets = Nil
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		for(sr <- registeredServices) {
			sr.unregister();
			println("u")
		}
	}

	private var pendingRootResources: List[() => Object] = Nil
	private var pendingTypeHandler: List[() => Object] = Nil
	private var pendingRenderlets: List[() => Object] = Nil
	private var registeredServices: List[ServiceRegistration] = null
}