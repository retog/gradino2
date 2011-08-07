package org.wymiwyg.gradino.renderlets.html

import java.net.URLEncoder
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.scala.utils.RichGraphNode
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DC
import org.wymiwyg.gradino.Ontology


class HeadedItemRenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	override def getModePattern = null

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		 <title>{res/Ontology.title*}</title>
	</head>
	<body>
		<div class="zz-content">
			{render(res, "naked")}
		</div>
	</body>
</html>		
			}
		}
	}

}
