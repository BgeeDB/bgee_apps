#!/bin/sh

#note that the file owltools-oort-all.jar in the owltools project is located in OWLTools-Oort/bin/
mvn install:install-file -Dfile=source_libraries/owltools-oort-all.jar -DgroupId=org.bbop -DartifactId=OWLTools-all -Dversion=0.2.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/
#Note that the file ncbi2owl.jar in the owltools project is located in OWLTools-NCBI/bin/
mvn install:install-file -Dfile=source_libraries/ncbi2owl.jar -DgroupId=org.bbop -DartifactId=OWLTools-ncbi -Dversion=0.2.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/
#Note that the file OWLTools-Sim.jar in the owltools project is located in OWLTools-Sim/target/ (was renamed to remove version info)
mvn install:install-file -Dfile=source_libraries/OWLTools-Sim.jar -DgroupId=org.bbop -DartifactId=OWLTools-sim -Dversion=0.2.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/

mvn install:install-file -Dfile=source_libraries/orthoxml.jar -DgroupId=sbc -DartifactId=orthoxml -Dversion=0.1b -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=repository/

exit 0
