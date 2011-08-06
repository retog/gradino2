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
class ItemZariaRenderlet extends SRenderlet {

	val getRdfType = Ontology.Item

	//override def getMediaType() = MediaType.TEXT_HTML_TYPE

	override def getModePattern = "zaria"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			val editLink = "edit/"+(res*)
			val tagString = ""
			override def content = {

    <html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Post</title>

<!-- Zaria -->
<link rel="/zaria/stylesheet" type="text/css" href="/zaria/style.css" />
<script language="javascript" type="text/javascript" src="/zaria/zaria.js"></script>
<script language="javascript" type="text/javascript" src="/zaria/htmlparser.js"></script>

<!-- /Zaria -->
</head>
<body>

<h3>Post</h3>

<form method="post" action="/gradino/addpost"><!-- http://localhost/test2.php -->

  <label>Author </label><input type="text" name="makerName" value={res/FOAF.maker/FOAF.name*} />


	<label>URI</label><input type="text" name="uri" value={if (res.getNode.isInstanceOf[BNode]) {"<new>"} else {res*} } />

  <p><label>Title</label> <input type="text" name="title" value= {res/Ontology.title*} /></p>

  <p><label>Content</label></p>
	<textarea id="content-id" name="content" rows="15" cols="80" style="width: 80%">
 {res/Ontology.content*}
	</textarea>
	<br />
	<p>
	 <label>Tags</label> <input type="text" name="tags" value={ tagString } />
	  </p>
	  <p>
	  <label>Date</label><input type="text" name="date" value= {res/DC.date*} />
</p>
	<input type="submit" value="Submit" />
</form>
<script>

			var foo = {{
		layout: '<div class="zariaToolbar">[bold][italic][underline]</div>[edit-area]',
		buttons: [
			{{name:'bold', label:'Bold', cmd:'bold', className:'bold'}},
			{{name:'italic', label:'Italic', cmd:'italic', className:'italic'}},
			{{name:'underline', label:'Underline', cmd:'underline', className:'underline'}}
		]
	}};

			var options = {{
				layout: '<div class="zariaToolbar">[bold][italic][underline]</div>[edit-area]',
		buttons: [
			{{name:'bold', label:'Bold', cmd:'bold', className:'bold'}},
			{{name:'italic', label:'Italic', cmd:'italic', className:'italic'}},
			{{name:'underline', label:'Underline', cmd:'underline', className:'underline'}}
		]
				/*layout: "<div class='zariaToolbar'>[bold][italic][underline][justify-left][justify-full][justify-center][justify-right][unordered-list][ordered-list][font][size][link][unlink][html]</div>[edit-area]",
				buttons: [
						{{name:'bold', label:'Bold', cmd:'bold', className:'bold'}},
						{{name:'italic', label:'Italic', cmd:'italic', className:'italic'}},
						{{name:'underline', label:'Underline', cmd:'underline', className:'underline'}},
						//{{name:'justify-left', label:'Align Left', cmd:'justifyleft', className:'justifyleft'}},
						//{{name:'justify-full', label:'Justify', cmd:'justifyfull', className:'justifyfull'}},
						{{name:'justify-center', label:'Align Center', cmd:'justifycenter', className:'justifycenter'}},
						{{name:'justify-right', label:'Align Right', cmd:'justifyright', className:'justifyright'}},
						{{name:'unordered-list', label:'Unordered List', cmd:'insertunorderedlist', className:'insertunorderedlist'}},
						{{name:'ordered-list', label:'Ordered List', cmd:'insertorderedlist', className:'insertorderedlist'}},
						{{name:'font', label:'Font', cmd:'fontname', menu:[{{label:'Arial', value:'Arial'}},{{label:'Courier', value:'Courier'}},{{label:'Times New Roman', value:'Times New Roman'}}]}},
						{{name:'size', label:'Size', cmd:'fontsize', menu:[{{label:'12pt', value:'3'}},{{label:'24pt', value:'6'}},{{label:'36pt', value:'7'}}]}},
						{{name:'link', label:'Link', cmd:'createlink', className:'link', prompt:"Enter your URL: "}},
						{{name:'unlink', label:'Unlink', cmd:'unlink', className:'unlink'}},
						{{name:'html', label:'html toggle', toggleMode: true, className:'html'}}

				]*/
			}};

			/*var defaultArea = new Zaria('default-id');
			function getDefault() {{
				defaultArea.syncContent();
				alert(document.getElementById('default-id').value);
			}};*/

			alert(document.getElementById('content-id').value);
			var content = new Zaria('content-id', foo);//Zaria.defaults);//, options);
			function getContent() {{
				content.syncContent();
				alert(document.getElementById('content-id').value);
			}}
		</script>

</body>
</html>
			}
		}
	}

}