This is a local Maven repository, used to store libraries not available from a remote repository.
See mvn-install-file.sh to see libraries already installed, or to configure new libraries to install.

mvn-install-file.sh should be run from the directory where it is located (m2_local_repository/).
The libraries to install should be placed in the directory source_libraries/ (m2_local_repository/source_libraries/)
They will be installed in m2_local_repository/repository/.

The libraries stored in m2_local_repository/source_libraries/ should be added to the SVN.
The folder m2_local_repository/repository/ is present on the SVN, but not its content,
as it is installed using mvn-install-file.sh and the content of m2_local_repository/source_libraries/,
which is stored on the SVN.


log4jdbc-log4j2 1.17-SNAPSHOT
-----------------------------

The root pom.xml depends on org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.17-SNAPSHOT.
That version is not published to Maven Central or Sonatype snapshots. It must be built and
installed locally once per machine (or into this repository directory).

Source: https://github.com/brunorozendo/log4jdbc-log4j2

Option 1 — install into ~/.m2 (simplest):

  git clone https://github.com/brunorozendo/log4jdbc-log4j2.git
  cd log4jdbc-log4j2
  mvn install -DskipTests

Option 2 — install into this project's local repository (used by bgee-local-repository in pom.xml):

  git clone https://github.com/brunorozendo/log4jdbc-log4j2.git
  cd log4jdbc-log4j2
  mvn install -DskipTests -Dmaven.repo.local=/path/to/bgee_apps/m2_local_repository/repository

Replace /path/to/bgee_apps with the absolute path to this repository's parent directory.

If a previous Maven run cached a resolution failure, remove the stale metadata before building
bgee_apps:

  rm -rf ~/.m2/repository/org/bgee/log4jdbc-log4j2/log4jdbc-log4j2-jdbc4.1/1.17-SNAPSHOT/*.lastUpdated
  rm -rf ~/.m2/repository/org/bgee/log4jdbc-log4j2/log4jdbc-log4j2-jdbc4.1/1.17-SNAPSHOT/resolver-status.properties

Then run Maven with forced updates, e.g. mvn -U clean install
