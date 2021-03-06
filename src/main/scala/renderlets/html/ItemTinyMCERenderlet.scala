package org.wymiwyg.gradino.renderlets.html

import org.wymiwyg.gradino.Ontology
import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.xml.sax.SAXParseException
import org.apache.clerezza.rdf.ontologies.{DC, FOAF}
import javax.ws.rs.core.MediaType

/**
 * A Renderlet for rss:items
 */
class ItemTinyMCERenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	//override def getMediaType() = MediaType.TEXT_HTML_TYPE

	override def getModePattern = "tiny"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			val editLink = "edit/"+(res*)
			val tagString = ""
			override def content = {

    <html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Edit Post</title>

<!-- TinyMCE -->
<script type="text/javascript" src="/tinymce/jscripts/tiny_mce/tiny_mce.js"></script>
<script type="text/javascript">
	tinyMCE.init({{ mode : "textareas", theme : "advanced" }});
</script>
<!-- /TinyMCE -->

</head>
<body>

<h3>Tyni Edit Post</h3>

<form method="post" action="/gradino/addpost"><!-- http://localhost/test2.php -->

  <label>Author </label><input type="text" name="makerName" value={res/FOAF.maker/FOAF.name*} /><br/>


	<label>URI</label><input type="text" name="uri" size="90" value={if (res.getNode.isInstanceOf[BNode]) {"<new>"} else {res*} } />

  <p><label>Title</label> <input type="text" name="title" value= {res/Ontology.title*} size="90"/></p>

  <p><label>Content</label></p>
	<!-- Gets replaced with TinyMCE, remember HTML in a textarea should be encoded -->
	<textarea id="content" name="content" rows="15" cols="80"
		style="width: 80%">{res/Ontology.content*}</textarea>
	<br />
	<p>
	 <label>Tags</label> <input type="text" name="tags" value={ tagString } />
	  </p>
	  <p>
	  <label>Date</label><input type="text" name="date" value= {res/DC.date*} />
</p>
	<input type="submit" value="Submit" />
</form>

</body>
</html>
			}
		}
	}

}