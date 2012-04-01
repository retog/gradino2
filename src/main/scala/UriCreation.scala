package org.wymiwyg.gradino

import org.apache.clerezza.osgi.services.ServicesDsl
import org.slf4j.scala.Logging
import org.osgi.framework.BundleContext
import org.apache.clerezza.platform.config.PlatformConfig
import java.util.Date
import java.text.SimpleDateFormat
class UriCreation(context: BundleContext) extends Logging {

	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._

	private def baseUri = {
		val pc = $[PlatformConfig]
		if (pc != null) {
			pc.getDefaultBaseUri.getUnicodeString
		} else {
			logger.warn("Couldn't access PlatformConfig")
			"http://localhost:8080/"
		}

	}
	
	def buildUri(title : String) : String = {
        val now = new Date();
        val sdf = new SimpleDateFormat("yyyy/MM/dd/");

		var uri = baseUri+ sdf.format( now )
		var stub = title.replaceAll(" ","-").replaceAll("\\]","").replaceAll("\\[","")
		uri = uri+stub
		// System.out.println("URI for POSTed item = "+uri)
		return uri
	}

}