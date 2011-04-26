package org.farewellutopia.blog

import org.apache.clerezza.rdf.core.UriRef

object Ontology {

	val title = new UriRef("http://purl.org/rss/1.0/title");
	val content = new UriRef("http://planetrdf.com/ns/content");
	val taggedWith = new UriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag")

	final val LatestItemsPage_String = "http://farewellutopia.com/blog#LatestItemsPage"
	val LatestItemsPage = new UriRef("http://farewellutopia.com/blog#LatestItemsPage")

}
