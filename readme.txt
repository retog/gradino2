Compililing and launching

To compile gradino2 run

mvn install

to run gradino start clerezza and enter on the clerezza-console

start("mvn:org.wymiwyg/gradino")

Now access /blog on you host, e.g. http://localhost:8080/blog

Styling the blog:
The style can adapted either by overwriting a css or by creating a new renderlet,
the second approach allows to create a different HTML representation for a given
RDF resource.

To overwrite a style you simply put a file at the respective path beneath
src/main/resources/CLEREZZA-INF/web-resources, the bundle containing this resource
must have a higher startlevel than the bundle providing the original file.

The code contains an example of Renderlet changing the appearance of most resources,
the class org.wymiwyg.gradino.html.ResourceRenderlet is an example of such a style,
it's activation is commented out in the Activator class.


About this project:

The original intention was to write a minimalistic blog engine based on
Apache Clerezza for the P2 course.

To have some real data to play with I asked Danny Ayers to get a dump of the
data of his blog. As a first step I decided to write a clone of his blogging
engine replicating the styles. Danny was quite happy about the additional
possibilities of the underlying Clerezza platform and suggested this to be
gradino 2, i.e. the basis for the next version of his blogging software.

Apart from some tiny bits to show how things can be migrated the code has been
written from scartch and is based on the Clerezza RDF api and not on the Jena
API as gradino 1. For information on the original gradino: http://hyperdata.org/wiki/wiki/Gradino