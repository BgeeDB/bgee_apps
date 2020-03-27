# Bgee application source code of Bgee release 14.1

1. [Directory structure](#directory-structure)
2. [To do for each release of Bgee](#to-do-for-each-release-of-bgee)

## Directory structure

```
bgee-applications/      The Java Maven project of Bgee
....bgee-core/          The core layer of Bgee, or "business" layer.
....bgee-dao-api/       The API to use a data source, such as a MySQL database. This API
                        is used by the "bgee-core" and the "bgee-pipeline" modules.
....bgee-dao-sql/       A service provider for the "bgee-dao-api" module, allowing to use
                        a MySQL database as data source.
....bgee-pipeline/      Classes used to generate and insert data into the Bgee database.
                        The jar file generated from this module will be used as part of the pipeline,
                        see pipeline directory description.
....bgee-webapp/        The controller and view layer of the application. This module
                        relies only on the use of the "bgee-core" module. It is not
                        aware of the use under the hood of the other modules.
```

## To do for each release of Bgee

The `master` branch always reflects the release of Bgee on production, the `develop` branch
reflects the next release of Bgee.

### Git

* Before merging `develop` into `master`:
  * from `master` creates two new branches for archiving:
    * a branch `archive_bgee_vXX` (e.g., `archive_bgee_v14`). This will be the official branch
    for this archived version, where specific changes for this version can still be done.
    In this archive branch, and only in this branch, update the properties (
    `bgee-webapp/src/main/resources/bgee.properties`,
    `bgee-webapp/src/main/resources/bgee.dao.properties`) so that, for instance,
    links to download files point to the archived version.
    * a branch `archive_common_bgee_vXX_and_later` (e.g., `archive_common_bgee_v14_and_later`),
    that will be a branch allowing to deploy fixes that need to impact all versions starting
    with this one (for instance, if a vulnerability is discovered in this version, and impacts
    all further versions, the fix will be done in this branch, and be merged back into the branches
    for all other versions, including `archive_bgee_vXX`, `develop`, `master`, etc).
  * Make a git tag for the version in `master` if it has changed since the last tag was made.
  * Rebuild a WAR file from `master` if any change occurred since the last deployment of the webapp,
  to have a clean WAR file for deploying what is going to be the next archive version.
  * Update the news in the `develop` branch.
  * Check the values of the properties in the `develop` branch
* Then merge `develop` into `master`
* Create a new git tag for this new release in `master`

### Servers

On all production/backup servers:

* load the MySQL dump of the database generated from the development server
* recreate the directories used by the webapp, for the new release: `/var/bgee/webapp/XX/`,
`/var/bgee/webapp/XX/requestparameters/`,
`/var/bgee/webapp/XX/topanat/`, `/var/bgee/webapp/XX/topanat/results`. For instance, create the directories
`/var/bgee/webapp/14_1/`, `/var/bgee/webapp/14_1/requestparameters/`,
`/var/bgee/webapp/14_1/topanat/`, `/var/bgee/webapp/14_1/topanat/results/`
* Copy the content of the directory where `RequestParameters` are stored (change in this class should be
backward compatible), for making them available for the new release. If it's not backward compatible,
then all URLs using the `data` parameter will be broken, but maybe sometimes it will be necessary.
For instance, copy the content of `/var/bgee/webapp/14/requestparameters/` into
`/var/bgee/webapp/14_1/requestparameters/`.
* Do NOT copy the content of the `topanat` directory from the previous version. Computations need to be redone
with the new data.

For Tomcat:

* Install the new version of the webapp in the tomcat folder
* make the `bgee_latest` symlink to point to it. `bgee_latest` is defined as the ROOT in tomcat configuration
* Change the `bgee.properties` file of the previous release to switch it to archive mode (
`org.bgee.webapp.archive=true`).
* Apache server should be updated so that the permalinks of the new release redirect to the root of the server
(so that, for instance, `https://bgee.org/bgee14_1/` redirects to `https://bgee.org/`). This redirection
should be stopped for the previous release of course.
* Apache should also be configured for redirecting URLs of the SPARQL endpoint in a similar way
(e.g., `https://bgee.org/sparql14_1` should redirect to `https://bgee.org/sparql`).

Once everything is ready:

* On all servers, then visit the TopAnat examples from the webinterface so that they are already
pre-computed for users.