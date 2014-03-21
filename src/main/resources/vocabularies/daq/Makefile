# daQ – Data Quality Vocabulary
#
# Authors:
# Christoph Lange <math.semantic.web@gmail.com>
# Jeremy Debattista <jerdebattista@gmail.com>
#
# © 2014–

# First adapt Makefile.vars.template and copy it to Makefile.vars
include Makefile.vars

# This requires rdfcat from Jena to be installed (https://jena.apache.org/documentation/javadoc/jena/jena/rdfcat.html)
daq.rdf: daq.trig
	< $< java -cp $(RDFCAT.CLASSPATH) jena.rdfcat -n - -out RDF/XML > $@

