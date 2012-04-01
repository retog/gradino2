package org.wymiwyg.gradino.renderlets.html

import org.wymiwyg.gradino.Ontology
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.RendererFactory
import org.apache.clerezza.platform.typerendering.scala._
import org.xml.sax.SAXParseException
import org.apache.clerezza.rdf.ontologies.{DC, DCTERMS, FOAF, SIOC}
import javax.ws.rs.core.MediaType
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.osgi.services.ServicesDsl
import org.osgi.framework.BundleContext
import org.apache.clerezza.rdf.ontologies.SKOS
import org.apache.clerezza.platform.typerendering.ResultDocModifier


/**
 * A Renderlet for rss:items
 */
class ItemCreateRenderlet(context: BundleContext) extends SRenderlet {

	private val servicesDsl = new ServicesDsl(context)
	import servicesDsl._
	val getRdfType = SIOC.Post

	override def getMediaType() = MediaType.TEXT_HTML_TYPE

	override def getModePattern = "create"
	  
	private def getSelectedSubjectXML(label: String, uri: String) = <div class="subject">
		{label}
		<input type="hidden" name="subject" value={uri} />
	  </div>

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			val editLink = "edit/"+(res*)
			override def content = { 

   <html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta charset="utf-8" />
    <title>{res/DCTERMS.title*} (create.js)</title>
    <script type="text/javascript" src="/create/deps/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="/create/deps/jquery-ui-1.8.18.custom.min.js"></script>
    <script type="text/javascript" src="/create/deps/modernizr.custom.80485.js"></script>
    <script type="text/javascript" src="/create/deps/underscore-min.js"></script>
    <script type="text/javascript" src="/create/deps/backbone-min.js"></script>
    <script type="text/javascript" src="/create/deps/vie-min.js"></script>

    <!-- rdfQuery and annotate are only needed for the Hallo
    annotations plugin -->
    <script type="text/javascript" src="/create/deps/jquery.rdfquery.min.js"></script>
    <script type="text/javascript" src="/create/deps/annotate-min.js"></script>

    <script type="text/javascript" src="/create/deps/hallo.js"></script>
    <script type="text/javascript" src="/create/examples/create.js"></script>
    <script type="text/javascript">
      jQuery(document).ready(function () {{
        jQuery('body').midgardCreate({{
          url: function () {{
            return '/gradino/backbone';
          }},
          stanbolUrl: 'http://dev.iks-project.eu:8081' 
        }});
      }});
      
      /*Backbone.sync =function(method, model, options) {{
        alert('foo: '+method)
        alert('model: '+model.toJSON().toString())
        alert('model-URL: '+model.URL)
      }}*/
    </script>
    <link rel="stylesheet" href="/create/examples/font-awesome/css/font-awesome.css"
    />
    <link rel="stylesheet" href="/create/themes/create-ui/css/create-ui.css"
    />
    <link rel="stylesheet" href="/create/themes/midgard-notifications/midgardnotif.css"
    />
    <link rel="stylesheet" href="/create/examples/demo.css" />

<!-- concept finder stuff -->
    <link href="/create/examples/font-awesome/css/font-awesome.css" rel="stylesheet"/>
    <link href="/create/themes/create-ui/css/create-ui.css" rel="stylesheet"/>
    <link href="/create/themes/midgard-notifications/midgardnotif.css" rel="stylesheet"/>
    <link href="/create/examples/demo.css" rel="stylesheet"/>
  	<link href="/yui/2/container/assets/container-core.css" rel="stylesheet" type="text/css"/>
	<script src="/scripts/ajax-options.js" type="text/javascript"/>
	<script src="/scripts/status-message.js" type="text/javascript"/>
	<script src="/concepts/generic-resource/scripts/concept-find-create.js" type="text/javascript"/>
	<script src="/concepts/generic-resource/scripts/jquery.rdfquery.core-1.0.js" type="text/javascript"/>
	<script src="/scripts/alert-message.js" type="text/javascript"/>
	<script src="/yui/2/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"/>
	<script src="/yui/2/element/element-min.js" type="text/javascript"/>
	<script src="/yui/2/container/container-min.js" type="text/javascript"/>
<script type="text/javascript">
ConceptFinder.setAddConceptCallback(function(label,uri) {{
	var section = '&lt;div class="subject"&gt; 		'+label+' 		&lt;input value="'+uri+'" type="hidden" name="subject"&gt;&lt;/input&gt; 	  &lt;/div&gt;'
	$('#concepts-id-form-section').append(section)
}})
</script>
  </head>
  
  <body>
    <div xmlns:sioc="http://rdfs.org/sioc/ns#" xmlns:dcterms="http://purl.org/dc/terms/"
    about="http://example.net/blog/" rel="dcterms:hasPart" rev="dcterms:partOf">
      <article typeof="sioc:Post" about={if (res.getNode.isInstanceOf[UriRef]) res* else "urn:x-magic:new"}>
        <h1 property="dcterms:title">{if ((res/DCTERMS.title).size > 0) res/DCTERMS.title* else "[no title]"}</h1>
        <div property="sioc:content">{if ((res/SIOC.content).size > 0) res/SIOC.content* else "[no content]"}</div>
      </article>
	  <p>
	{
	 /* 	def xhtmlRender(resource: GraphNode, mode: String) = {
		  def parseNodeSeq(string: String) = {
			  _root_.scala.xml.XML.loadString("<elem>" + string + "</elem>").child
		  }
		  val baos = new java.io.ByteArrayOutputStream
		  //context.
		  renderer.render(resource, context, mode, baos)
		  parseNodeSeq(new String(baos.toByteArray))
	   }

	
	  
	  xhtmlRender(res,"concept-find-create-naked")
	  */
	  /*val rf: RendererFactory= $[RendererFactory]
	  if (rf == null) {
	    throw new RuntimeException("No RendererFactory!")
	  }*/
	    val monitor = this
	var inner: Seq[scala.xml.Node] = null
		servicesDsl.doWith[RendererFactory] ( (rf: RendererFactory) => {
			 def parseNodeSeq(string: String) = {
				  _root_.scala.xml.XML.loadString("<elem>" + string + "</elem>").child
			  }
			import scala.collection.JavaConversions._
			//val sList : List[]
			val r = rf.createRenderer(res,"concept-find-create-naked",List(MediaType.APPLICATION_XHTML_XML_TYPE))
			val baos = new java.io.ByteArrayOutputStream
			r.render(res, context, "concept-find-create-naked", uriInfo, requestHeaders, responseHeaders, sharedRenderingValues, baos)
	  		val stringContent = new String(baos.toByteArray)
			println("finally got "+stringContent)
	  		inner = parseNodeSeq(stringContent)
	  		this.synchronized {
				 monitor.notifyAll()
			}
	  })
	  while (inner == null) {
	    synchronized {
	      inner.wait(10)
	    }
	  }
	 inner
	  
}
	</p>
<div id="concepts-id-form-section">
	  {for (concept <- res/DC.subject) yield getSelectedSubjectXML(concept/SKOS.prefLabel*, concept*)}
</div>
			  
      <article typeof="http://rdfs.org/sioc/ns#Post" about="http://example.net/blog/second_news_item">
        <h1 property="dcterms:title">Another item</h1>
        <div property="sioc:content">
          <p>Hello, worldüü!</p>
        </div>
      </article>
    </div>
  </body>
</html>

			}
		}
	}

}