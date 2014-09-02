#!/bin/sh

#proper file: OWLTools-Runner/bin/owltools-runner-all.jar
rm -rf ~/.m2/repository/org/bbop/
mvn install:install-file -Dfile=source_libraries/owltools-runner-all.jar -DgroupId=org.bbop -DartifactId=OWLTools-all -Dversion=0.2.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/
#Note that the file ncbi2owl.jar in the owltools project is located in OWLTools-NCBI/bin/
mvn install:install-file -Dfile=source_libraries/ncbi2owl.jar -DgroupId=org.bbop -DartifactId=OWLTools-ncbi -Dversion=0.2.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/

rm -rf ~/.m2/repository/sbc/orthoxml/
mvn install:install-file -Dfile=source_libraries/orthoxml.jar -DgroupId=sbc -DartifactId=orthoxml -Dversion=0.1b -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/

exit 0
