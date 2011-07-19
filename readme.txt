Compililing and launching

To compile gradino2 run

mvn install

to run gradino start clerezza and enter on the clerezza-console

start("mvn:org.wymiwyg/gradino")

Now access /blog on you host, e.g. http://localhost:8080/blog

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