# Settings

Information about setting some of the parameters used in `pom.xml`, `src/main/resources/bgee.properties`, `src/main/resources/bgee.dao.properties`,
`src/main/webapp/WEB-INF/web.xml`.

There are parameters:

* that we do not want to expose (such as database connection parameters)
* and/or for which it would be incorrect to provide a default value for external users of the source code
(e.g., email address to send mails from, directories to store analysis results).
* and/or that vary between production and development environments (e.g., the server root URL)

These parameters are defined in a Maven settings.xml configuration file (in `~/.m2 directory`, or `MAVEN_HOME/conf directory`, e.g., `/usr/share/maven/conf`).
An example configuration file is provided in `maven_settings_example.xml`.

Otherwise, parameters with default values are directly set in `src/main/resources/bgee.properties` and `src/main/resources/bgee.dao.properties`.


# Documentation

This page documents the use of the module `bgee-webapp`. Table of content: 

* [Deploy webapp on server](#deploy-webapp-on-server)
  * [configuration](#configuration)
  * [Maven WAR file build](#maven-war-file-build)
  * [Eclipse embedded deployment](#eclipse-embedded-deployment)
* [Java layout](#java-layout)
  * [Introduction](#java-layout)
  * [Request parameters](#request-parameters)
  * [View layer](#view-layer)
  * [Controller layer](#controller-layer)
  * [Bgee properties](#bgee-properties)
* [Javascript layout](#javascript-layout)
  * [Introduction](#javascript-layout)
  * [Common events and vars](#common-events-and-vars)
  * [Request parameters](#js-request-parameters)
  * [Bgee properties](#js-bgee-properties)
* [CSS layout](#css-layout)

## Deploy webapp on server

### Configuration 

* You might want to edit the Bgee properties in `BGEE_APPS_HOME/bgee-webapp/src/main/resources/bgee-webapp.properties`. This would usually be done only by the Bgee team, or only if you want to deploy your own Bgee server.

* You might want to edit the Maven configuration file `BGEE_APPS_HOME/bgee-webapp/pom.xml`, notably to edit the properties `css.version` and `js.version`. This allows to automatically append a suffix to javascript and/or css files, to force the update of the files in client cache (this allows to change this suffix at each Bgee release). This is usually done only for official releases.

* You need to configure the database connection parameters. To do that, you need to add a profile in your Maven `settings.xml` file (`~/.m2` directory, or `MAVEN_HOME/conf` directory, e.g., `/usr/share/maven/conf`), defining the properties `filter.dao.jdbc.url`, `filter.dao.jdbc.username`, and `filter.dao.jdbc.password`, used in `bgee-webapp/src/main/resources/bgee.dao.properties` (because for now, the webapp doesn't use a DataSource). It is recommended to make this profile deactivated when used within Eclipse (in case you want to deploy the webapp within Eclipse using a test database). See example below.

* Similarly, you need to configure the parameters for TopAnat (directories where to write results, location of R executable...). This should also be configured through profiles in your Maven `settings.xml` file, this will allow to define some properties in `bgee-webapp/src/main/resources/bgee.properties`. See example below. 

* You need to install R, and to create the directories needed by TopAnat. Corresponding properties: `org.bgee.core.topAnatRScriptExecutable` (e.g., `/usr/bin/Rscript`), `org.bgee.core.topAnatRWorkingDirectory` (e.g.; `/var/bgee/topanat/results`), and `org.bgee.core.topAnatResultsWritingDirectory` (e.g., `/var/bgee/topanat/results`)

* Example Maven `settings.xml` file is provided in `maven_settings_example.xml` (see use of profile `bgee-webapp-main`). 


### Maven WAR file build

* Build the webapp and all the modules it depends on, to generate the Bgee webapp WAR file in `BGEE_APPS_HOME/bgee-webapp/target/`. To create the WAR file, skipping integration tests and javadoc generation: 

```
mvn clean install -DskipITs -Dmaven.javadoc.skip=true --projects bgee-dao-api,bgee-dao-sql,bgee-core,bgee-webapp
```

(If the other modules were not modified, you can only build the `bgee-webapp` module if you want)

* You can then deploy the WAR file through a tomcat manager, or directly in a tomcat `webapp` directory: `TOMCAT_HOME/webapps`. If your sever is configured to autodeploy WAR files (default), there is nothing more to do, otherwise, you can unpack the WAR files using the command `jar -xvf yourWARfileName.war`, and relaunch the server.

### Eclipse embedded deployment 

* You can run the webapp directly from within Eclipse, however, you cannot use the embedded Eclipse tomcat server: our webapp uses some `Filter`s in `WEB-INF/web.xml` that are part of the regular distribution of tomcat since version 7, but not part of the basic Eclipse embedded server. 
* To point Eclipse to a regular Tomcat installation: in the menu `Window` > `Show View` > `Servers`. Right click on this view > `new`. Add path to your local installation of tomcat.
* To run the webapp in Eclipse, right click on the module `bgee-webapp`, then `Run As` > `Run on Server`. The webapp is now accessible at `http:localhost:8080/` (or other location if you modified the default configuration when adding the server). 
* You can also use the `Servers view` to avoid the `Run on server` procedure:    
  * In the menu `Window` > `Show View` > `Servers`. In this new view you can stop and relaunch the server, without having a new browser window opening each time.
  * Often, when modifying static files, such as javascript or css files, Eclipse does not rebuild the module, and the modifications are not seen after relaunching the server. In that case, right click on the module `bgee-webapp`, `Refresh`, then launch the server.
* The webapp needs to have access to a Bgee database. To configure the connection parameters to use when launching the webapp in Eclipse, you need to define test values for the properties `filter.dao.jdbc.url`, `filter.dao.jdbc.username`, `filter.dao.jdbc.password`, and `bgee.dao.jdbc.driver.names`, used in `bgee-webapp/src/main/resources/bgee.dao.properties`. Edit your Maven `settings.xml` file with a profile activated when `m2e` is used (`~/.m2` directory, or `MAVEN_HOME/conf` directory, e.g., `/usr/share/maven/conf`), an example Maven `settings.xml` file is provided in `maven_settings_example.xml` (see use of profile `bgee-webapp-test`). 


## Java layout

The Bgee applications follow a MVC pattern. The module `bgee-webapp` contains the `controller` and `view` layers, while the `model` layer is developed as a distinct API in the module `bgee-core`.


### Request parameters

#### Basic details

A class is used to communicate between the `controller` and the `view` layers of `bgee-webapp`: `org.bgee.controller.RequestParameters`. This class is responsible for: i) extracting parameters from URLs, or, more precisely, from `HttpServletRequest` objects (feature used in the `controller` layer); ii) generating URLs based on provided parameters (feature mostly used in the `view` layer, see the `getRequestURL` methods). 

Example: 

```java
//retrieve parameters from an HttpServletRequest object
RequestParameters rp = new RequestParameters(request, new URLParameters(), 
    BgeeProperties.getBgeeProperties(), true, "&");
//read values
rp.getPage();
rp.getAction();

//set parameters to generate an URL
RequestParameters urlGenerator = new RequestParameters(new URLParameters(), 
    BgeeProperties.getBgeeProperties(), true, "&");
urlGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
urlGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
urlGenerator.getRequestURL();
```

#### More details

The class `org.bgee.controller.URLParameters` stores the allowed parameters, and the order in which they should be stored in URLs. These parameters are represented as objects of the class `org.bgee.controller.URLParameters.Parameter`, allowing to define their type, accepted format, max length, etc. These parameters are then used by `RequestParameters` objects to read/write parameters in URLs. 

The notable allowed parameters defined in `URLParameters` are returned by the methods: 

* `getParamPage`: return the `Parameter` object representing the 'page' parameter, allowing to specify which `Page Controller` should handle the query (see [Controller section](#controller-layer)).
* `getParamAction`: return the `Parameter` object representing the 'action' parameter, allowing to specify which action should be performed by the `Page Controller` handling the query.
* `getParamDisplayType`:  return the `Parameter` object representing the 'display type' parameter, allowing to specify which `View` should generate the response to client (e.g., an HTML view, a JSON view).

For these parameters (and for others), there exist helper methods to get/set their values in `RequestParameters` objects: 

* `getPage`/`setPage`
* methods allowing to determine the category of the 'page' parameter (useful for selecting a `Page Controller`): `isTheHomePage`, `isADownloadPageCategory`, `isATopAnatPageCategory`, ...
* `getAction`/`setAction`
* `setDisplayType`
* methods allowing to determine the requested view (there is no `getDisplayType` method): `isXmlDisplayType`, `isJsonDisplayType`, `isCsvDisplayType`, `isTsvDisplayType`. If all these methods return `false`, then the requested view is HTML (default view).

There are other such helper methods available. In any case, `RequestParameters` can read/write values for any `Parameter`, even in the absence of helper methods, see the methods `getValues`, `gertFirstValue`, `addValue`, `resetValues`.

### View layer

#### Basic details

The `view` layer is responsible for generating responses from the server. The Bgee webapp can generate responses of several types: html, xml, json, csv/tsv. Each of these types correspond to a specific package, with a specific factory, and specific view implementations: `org.bgee.view.html`, `org.bgee.view.xml`, `org.bgee.view.json`, `org.bgee.view.dsv`.

All classes generating responses to client extend the class `org.bgee.view.ConcreteDisplayParent`, whatever the package they belong to is. Responses are written directly by calling its methods `writeln` and `write`. There is no use of, for instance, JSP language. 

Example: 

```java
//in a HTML view
this.writeln("<h1>Bgee: Gene Expression Evolution</h1>");
this.writeln("<a href='https://sib.swiss/' target='_blank' rel='noopener' "
    + "title='Link to the SIB Swiss Institute of Bioinformatics'>SIB</a>");

//in a JSON view
this.writeln(
        "{\"error\": {"
            + "\"code\": 404, "
            + "\"message\": \"We could not understand your query, "
            + "see details : " + message + "\""
        + "}}");
```

#### More details

The `view` layer uses an abstract factory pattern to provide requested views. To obtain a view, it is necessary: 

* to use the class `org.bgee.view.ViewFactoryProvider` to obtain a `ViewFactory`, through a call to a method `getFactory`. Each view package implements its own `ViewFactory` (e.g., `org.bgee.view.html.HtmlFactory`, `org.bgee.view.json.JsonFactory`). Which specific `ViewFactory` is returned is determined by the `RequestParameters` object provided as argument of the `getFactory` method, or can be explicitly specified. The caller is not aware - and doesn't care - about which specific `ViewFactory` implementation is returned.
* to use the returned `ViewFactory` to obtain views (e.g., `org.bgee.view.DownloadDisplay` by calling `getDownloadDisplay`, `org.bgee.view.TopAnatDisplay` by calling `getTopAnatDisplay`). Each view package provides its own implementation of these views (e.g., `org.bgee.view.html.HtmlTopAnatDisplay`, `org.bgee.view.json.JsonTopAnatDisplay`). As the caller is not aware - and doesn't care - about which specific `ViewFactory` he is using, he also doesn't know which specific implementation of a view he is using.

Example: 

```java
RequestParameters rp = new RequestParameters(request, new URLParameters(), 
    BgeeProperties.getBgeeProperties(), true, "&");
ViewFactoryProvider provider = new ViewFactoryProvider(BgeeProperties.getBgeeProperties());
ViewFactory factory = provider.getFactory(response, rp);
DownloadDisplay view = factory.getDownloadDisplay();
view.displayDownloadHomePage();
```

### Controller layer

#### Basic details

The main class of the `controller` layer is `org.bgee.controller.FrontController`. All requests to the server are channeled through this class. The responsibility of this class is notably to determine which `Page Controller` should handle the request, and to delegate the processing of this request to it. 

This class notably loads a `RequestParameters` object from the current `HttpServletRequest` received, that is used to read the parameters from the request, and that will be provided to all `Page Controller`s and all `View`s used, for them to retrieve specific parameters needed to process the request. See [request parameters section](#request-parameters) for more details about `RequestParameters`.

Example: 

```java
//in FrontController
BgeeProperties props = BgeeProperties.getBgeeProperties();
RequestParameters rp = new RequestParameters(request, new URLParameters(), 
    props, true, "&");
ViewFactory viewFactory = new ViewFactoryProvider(props).getFactory(response, rp);

if (rp.isTheHomePage()) {
    CommandHome controller = new CommandHome(response, rp, props, viewFactory);
    controller.processRequest();
}
```

#### More details

This `RequestParameters` object loaded by the `FrontController` is used to determine which `Page Controller` should be used, by reading the 'page' parameter (see [request parameters section](#request-parameters)). There exist one `Page Controller` per page category (e.g., download page category, topAnat page category). All `Page Controller`s extend the class `org.bgee.controller.CommandParent` and implements a `processRequest` method. 

The `RequestParameters` object loaded by the `FrontController` is also used to determine the action to be performed by the `Page Controller`, through the 'action' parameter, and to choose which view should be used to render the data, through the 'display type' parameter (see [request parameters section](#request-parameters)).

### Bgee properties

`To be written.`

## Javascript layout

The javascript files are stored in `BGEE_APPS_HOME/bgee-webapp/src/main/webapp/js/`. This directory contains the Bgee javascript files, while the sub-directory `lib/` contains libraries, such as jQuery or AngularJS. In Eclipse, these files appear under `Deployed Resources` > `webapp`. 

Some files are always included in all pages, others are specific to a given type of pages (e.g., download files pages, documentation pages, topAnat pages). These javascript files to be included are defined in the HTML view layer of the Java application, by the `includeJs` method of each class implementing a HTML view.

The files always included are: `bgeeproperties.js`, `urlparameters.js`, `requestparameters.js`, and `common.js` (in that order). (Note that all these files could be automatically merged into a single JS file for building the WAR file, using a Maven plugin)

### Common events and vars

`common.js` is the file responsible for loading the features needed on all pages (e.g., mouseover on header menu), and loading some prototype extensions.

It also loads two global vars: 

* `GLOBAL_PROPS`, which is a `bgeeProperties` object, allowing to retrieve Bgee specific properties (see [Bgee properties section](#js-bgee-properties)). This `GLOBAL_PROPS` object is notably used by `requestParameters` objects to generate URLs (see [request parameters section](#js-request-parameters)), and allows to easily make changes impacting the whole application (e.g., cross-domain AJAX queries to a test server). 
* `CURRENT_REQUEST`, which is a `requestParameters` object, allowing to retrieve all valid parameters present in the URL at page loading (see [request parameters section](#js-request-parameters)). This allows to retrieve the original parameters, even if the URL is modified after page loading. 

### JS request parameters

#### Basic details

A class is used to read/write parameters in URL, the class `requestParameters`, defined in the file `requestparameters.js`.

This class is used to generate URLs (for instance, to perform an AJAX query, or to add a link into a page), or to read parameters from URLs.  Notable methods include `getRequestURL` and `getRequestHash`. This class mirrors the Java `RequestParameters` class, and they are kept in sync for the most part (this also explains presence of non-standard javascript in this file :p).

Example: 

```javascript
//get an URL to perform an AJAX query
var urlGenerator = new requestParameters();
urlGenerator.setPage(urlGenerator.PAGE_TOP_ANAT());
//set other parameters
...
//set display type
urlGenerator.setDisplayType(urlGenerator.DISPLAY_TYPE_JSON());
//call getRequestURL with true, this adds a parameter allowing the server 
//to detect an AJAX query
var url = urlGenerator.getRequestURL(true);

//generate the hash part of the URL to update window.location.hash
var urlGenerator = new requestParameters();
urlGenerator.setPage(urlGenerator.PAGE_TOP_ANAT());
//Parameters can be stored in the search part or in the hash part of URLs.
//When calling getRequestHash, all parameters provided to the requestParameters 
//will be put in the hash. 
window.location.hash = urlGenerator.getRequestHash();
//Note: to be able to put some parameters in the search part, some parameters 
//in the hash part, see the getRequestURL method.

//Retrieve parameters from the search part and hash part of an URL .
//Parameters can be read/write in both parts
var rp = new requestParameters(window.location.search + window.location.hash);
//read parameters
rp.getPage();
rp.getAction();
```
#### More details

The principles are exactly the same as for the Java layout. This part is mostly duplicated from the Java section. 

The class `urlParameters` (defined in the `urlparameters.js` file) stores the allowed parameters, and the order in which they should be stored in URLs. These parameters are represented as objects of the class `Parameter` (defined in the same file), allowing to define their type, accepted format, max length, etc. These parameters are then used by `requestParameters` objects to read/write parameters in URLs. 

The notable allowed parameters defined in `urlParameters` are returned by the methods: 

* `getParamPage`: return the `Parameter` object representing the 'page' parameter, allowing to specify which `Page Controller` should handle the query (see [Java Controller section](#controller-layer)).
* `getParamAction`: return the `Parameter` object representing the 'action' parameter, allowing to specify which action should be performed by the `Page Controller` handling the query.
* `getParamDisplayType`:  return the `Parameter` object representing the 'display type' parameter, allowing to specify which `View` should generate the response to client (e.g., an HTML view, a JSON view).

For these parameters (and for others), there exist helper methods to get/set their values in `requestParameters` objects: 

* `getPage`/`setPage`
* `getAction`/`setAction`
* `setDisplayType`

There are other such helper methods available. In any case, `requestParameters` can read/write values for any `Parameter`, even in the absence of helper methods, see the methods `getValues`, `gertFirstValue`, `addValue`, `resetValues`.

### JS Bgee properties

A class is used to store the properties specific to the Bgee webapp, the class `bgeeProperties`, stored in the file `bgeeproperties.js`. This notably defines the `host` and `path` parameters to generate URLs and perform AJAX queries. By default, they are the `host` and `path` of the webapp that generated the page (`location.host` and `location.pathname`), but they could be easily changed to perform queries to a different server (see the `getRequestURL` method of the class `requestParameters`).

## CSS layout

The CSS files are stored in `BGEE_APPS_HOME/bgee-webapp/src/main/webapp/css/`. In Eclipse, these files appear under `Deployed Resources` > `webapp`.

Some files are always included in all pages, others are specific to a given type of pages (e.g., download files pages, documentation pages, topAnat pages). These css files to be included are defined in the HTML view layer of the Java application, by the `includeCss` method of each class implementing a HTML view.

The file always included is: `bgee.css`.
