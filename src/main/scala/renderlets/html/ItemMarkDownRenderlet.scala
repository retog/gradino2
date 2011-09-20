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
class ItemMarkDownRenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	//override def getMediaType() = MediaType.TEXT_HTML_TYPE

	override def getModePattern = "markDown"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			val editLink = "edit/"+(res*)
			val tagString = ""
			override def content = {
			  //this is here to make sure its on top
				resultDocModifier.addScriptReference("/jquery/jquery-1.3.2.min.js")
				resultDocModifier.addScripts("""
ConceptFinder.setAddConceptCallback(function(label,uri) {
	alert(label+", "+uri)
	$('#concepts-id-form-section').append('<input type="hidden" name="subject" value='+uri+' />')
alert("added")
})
""")
    <html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Edit Markdown</title>
</head>
<body>

<h3>Post</h3>

<form method="post" action="/gradino/addpost"><!-- http://localhost/test2.php -->

  <label>Author </label><input type="text" name="makerName" value={res/FOAF.maker/FOAF.name*} />


	<label>URI</label><input type="text" name="uri" value={if (res.getNode.isInstanceOf[BNode]) {"<new>"} else {res*} } />

  <p><label>Title</label> <input type="text" name="title" value= {res/Ontology.title*} /></p>

  <p><label>Content</label></p>
	<textarea id="content-id" name="contentMarkDown" rows="15" cols="80"
		style="width: 80%">{res/Ontology.content*}</textarea>
	<br />
	<p>
	 <label>Tags</label> <input type="text" name="tags" value={ tagString } />
	  </p>
<p>
{render(res,"concept-tagging-naked")}
</p>
	  <p>
	  <label>Date</label><input type="text" name="date" value= {res/DC.date*} />
<div id="concepts-id-form-section">
	  {for (concept <- res/DC.subject) yield <input type="hidden" name="subject" value={concept*} />}
</div>
</p>
	<input type="submit" value="Submit" />
</form>
</body>
</html>
			}
		}
	}

}