This directory is used to store the JAR files of libraries not remotely available from a Maven repository. 

These libraries will be included into the local repository (applications/m2_local_repository/repository/) 
by the script mvn-install-file.sh (applications/m2_local_repository/mvn-install-file.sh)

The libraries placed in this folder should be added to the SVN, so that anyone can run mvn-install-file.sh 
to install the local repository. 