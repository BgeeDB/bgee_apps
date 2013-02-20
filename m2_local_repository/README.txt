This is a local Maven repository, used to store libraries not available from a remote repository.
See mvn-install-file.sh to see libraries already installed, or to configure new libraries to install. 

mvn-install-file.sh should be run from the directory where it is located (applications/m2_local_repository/). 
The libraries to install should be placed in the directory source_libraries/ (applications/m2_local_repository/source_libraries/)
They will be installed in applications/m2_local_repository/repository/. 

The libraries stored in applications/m2_local_repository/source_libraries/ should be added to the SVN. 
The folder applications/m2_local_repository/repository/ is present on the SVN, but not its content, 
as it is installed using mvn-install-file.sh and the content of applications/m2_local_repository/source_libraries/, 
which is stored on the SVN.
