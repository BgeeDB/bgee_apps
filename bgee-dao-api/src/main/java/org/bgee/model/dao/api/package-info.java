/**
 * This package provides the API to access data sources. It notably provides 
 * DAO interfaces, with methods returning or accepting data by means of 
 * {@code TransferObject}s. The entry point to this API is the class 
 * {@link org.bgee.model.dao.api.DAOManager DAOManager}, see its Javadoc for 
 * detailed explanations about its use. 
 * <p>
 * This API can be used with any concrete implementation acting under the hood, 
 * specific to a data source, using the {@code Service Provider} mechanisms (see below). 
 * As of Bgee 13, an implementation for MySQL is provided, in the module 
 * {@code bgee-dao-sql}.
 * <p>
 * A client of this {@code bgee-dao-api} module is notably 
 * the {@code bgee-core} module, that uses it to instantiate {@code Entity}s, 
 * using the {@code TransferObject}s obtained from DAOs. 
 * For convenience, the packages of this module follows the same organization 
 * than the packages related to {@code Entity}s in the {@code bgee-core} module.
 * <p>
 * This API provides: 
 * <ul>
 * <li>Interfaces for DAOs, that need to be implemented by the concrete implementations 
 * specific to a data source.
 * <li>{@code TransferObject}s, that are used by the concrete implementations 
 * to communicate between the client and the data source.
 * <li>A {@code DAOManager}, that: 
 *   <ul>
 *   <li>defines the methods to be implemented by the factory 
 *   of the concrete implementations, to obtain DAOs.
 *   <li>is responsible to instantiate and return a concrete factory, using parameters 
 *   provided by the client, and the {@code Service Provider} mechanism (see 
 *   below). The client is not aware of which concrete factory it obtains (besides  
 *   providing the parameters), and is only exposed to the abstract factory and interfaces 
 *   of this API. The code of the client is thus not dependent of any concrete 
 *   implementation. This follows the Abstract Factory Pattern.
 *   <li>provides methods to close or kill resources used. 
 *   </ul>
 * <li>{@code Exception}s that can be thrown by the concrete implementations. 
 * </ul>
 * <p>
 * <h3>Service Provider mechanism</h3>
 * This API supports the standard 
 * <a href='http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider'>
 * Service Provider</a> mechanism. Concrete implementations must include the file 
 * {@code META-INF/services/org.bgee.model.dao.api.DAOManager}. The file must contain 
 * the name of the implementation of {@code org.bgee.model.dao.api.DAOManager}. 
 * For example, to load the {@code my.sql.Factory} class, 
 * the {@code META-INF/services/org.bgee.model.dao.api.DAOManager} file 
 * would contain the entry:
 * <pre>my.sql.Factory</pre>
 * <p>
 * Important note about {@code ServiceLoader} and shared {@code ClassLoader} 
 * (like in tomcat): http://stackoverflow.com/a/7220918/1768736
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
package org.bgee.model.dao.api;