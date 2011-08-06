package org.wymiwyg.gradino.renderlets.html

import org.wymiwyg.gradino.Ontology
import scala.xml.XML
import java.util.Date
import java.text.DateFormat
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.xml.sax.SAXParseException
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.ontologies.RDFS
import org.apache.clerezza.rdf.scala.utils.RichGraphNode


/**
 * A Renderlet for rss:items
 */
class ResourceRenderlet extends SRenderlet {

	val getRdfType = RDFS.Resource

	override def getModePattern = null

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {

			override def content = {
				<html xmlns="http://www.w3.org/1999/xhtml">
				<head>
					<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
				<meta name="description" content="Danny Ayers' blog, mostly featuring things like Linked Data and the Semantic Web" />
				<meta name="keywords" content="Danny, Danny Ayers, Raw Blog, Linked Data, Semantic Web, RDF" />
					<title>Danny Ayers : Raw Blog</title>
				<link rel="stylesheet" type="text/css" href="/style/gradino/style.css"/>
				<link rel="stylesheet" href="http://hyperdata.org/css/tabs.css" type="text/css" />
				</head>
				<body id="tab1">

				<ul id="tabnav">
						<li class="tab1"><a href="/" title="Danny Ayers' Blog">Raw Blog</a></li>
						<li class="tab2"><a href="http://hyperdata.org/danja/" title="my updates on various social sites">Planet Danja</a></li>
						<li class="tab3"><a href="http://hyperdata.org/planet/" title="stuff I'm watching">Planet Raw</a></li>
						<li class="tab4"><a href="http://hyperdata.org/" title="Linked Data related material">hyperdata.org</a></li>
						<li class="tab5"><a href="http://hyperdata.org/wiki/" title="on my personal wiki">Projects</a></li>
						<li class="tab6"><a href="http://hyperdata.org/xmlns/" title="various namespaces/ontologies">Vocabs</a></li>
						<li class="tab7"><a href="http://danny.ayers.name/" title="Danny Ayers' Resume/CV">About Me</a></li>
				</ul>
				<p/>
				<div id="header">
				<h1 id="blog-title"><a href="/">Raw</a></h1>
				<p id="description">being the blog of <a href="http://danny.ayers.name">Danny Ayers</a></p>

				</div>
				<div id="content">
				<div id="main">

		{
			def iRender(n: RichGraphNode) = {
				if (mode == null) {
					render(n, "naked")
				} else {
					render(n, mode + "-naked")
				}
			}
			if ((res/RDF.first).size > 0) {
				for (n <- res!!) yield {
					iRender(n)
				}
			} else {
				iRender(res)
			}
		}
					</div>
<div id="sidebar">

<p>
<a href="/feed"><img src="/icons/feed-icon-14x14.png" title="Feed" alt="Feed" /></a>
</p>

<p>
Powered by <a href="http://hyperdata.org/wiki/wiki/Gradino">Gradino</a>
</p>
</div>

</div>


</body></html>
			}
		}
	}

}