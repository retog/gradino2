package com.farewellutopia.blog

import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import scala.collection.JavaConversions.asJavaDictionary
import scala.collection.mutable
import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

/**
 * A trait to facilitate creating bundle actovators to register service.
 *
 * This should be contributed to Clerezza.
 */
trait ActivationHelper extends BundleActivator {

	/**
	 * this is intended to be used exclusively in the argument to the register-methods
	 */
	protected var context: BundleContext= null

	/**
	 * Registers a JAX-RS Root Resource
	 */
	protected def registerRootResource(rootResource: =>Object) {
		registerService(rootResource, classOf[Object], "javax.ws.rs" -> true)
	}

	/**
	 * Register a Renderlet
	 */
	protected def registerRenderlet(renderlet: =>TypeRenderlet) {
		registerService(renderlet, classOf[TypeRenderlet])
	}

	/**
	 * Register a TypeHandler
	 */
	protected def registerTypeHandler(typeHandler: => Object) {
		registerService(typeHandler, classOf[Object], "org.apache.clerezza.platform.typehandler" -> true)
	}

	/**
	 * Register a service exposing a specified interface with an arbitrary number of
	 * arguments
	 */
	protected def registerService(instance: => AnyRef, interface:Class[_],
																arguments: (String, Any)*) {
		registerService(instance, Seq(interface), Map(arguments:_*))
	}

	/**
	 * Registers a service for a Seq of interfaces and a map of arguments
	 */
	protected def registerService(instance: => AnyRef, interfaces: Seq[Class[_]],
																arguments: Map[String, Any]) {
		managedServices ::= ((() => instance, interfaces, arguments))
	}

	/**
	 * invoked by the OSGi environment when the bundle is started, this method registers
	 * the services for which the register-methods hqave been called (during object construction)
	 */
	def start(context: BundleContext) {
		this.context = context
		registeredServices = Nil
		for (entry <- managedServices) {
			val args = asJavaDictionary(mutable.Map(entry._3.toSeq:_*))
			registeredServices ::= context.registerService(
				(for (interface <- entry._2) yield interface.getName).toArray, entry._1(), args)
		}
		this.context = null
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		for(sr <- registeredServices) {
			sr.unregister();
		}
		registeredServices = null
	}

	private var managedServices: List[(() => Any, Seq[Class[_]], Map[String, Any])] = Nil

	private var registeredServices: List[ServiceRegistration] = null
}