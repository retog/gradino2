package org.wymiwyg.gradino

import org.apache.clerezza.rdf.core.UriRef

object Ontology {

	val title = new UriRef("http://purl.org/rss/1.0/title");
	val content = new UriRef("http://planetrdf.com/ns/content");
	val taggedWith = new UriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag")

	final val LatestItemsPage_String = "http://wymiwyg.org/gradino#LatestItemsPage"
	val LatestItemsPage = new UriRef(LatestItemsPage_String)
	val Item = new UriRef("http://purl.org/rss/1.0/item")

	val BlogAdminPage = new UriRef("http://farewellutopia.com/blog#BlogAdminPage")


}
