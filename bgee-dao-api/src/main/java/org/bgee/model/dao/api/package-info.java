/**
 * This package provides the API to access data sources. It is completely independent 
 * from other packages and modules. It implements the 
 * {@link http://www.oracle.com/technetwork/java/dataaccessobject-138824.html 
 * J2EE Data Access Object Patterns}. It notably provides DAO interfaces, 
 * with methods returning or accepting data by means of <code>TransferObject</code>s. 
 * This API can then be used with any concrete implementation acting under the hood, 
 * specific to a data source. As of Bgee 13, an implementation for MySQL is provided, 
 * in the module <code>bgee-api-sql</code>.
 * <p>
 * A client of this <code>bgee-dao-api</code> module is notably 
 * the <code>bgee-core</code> module, that uses it to instantiate <code>Entity</code>s, 
 * using the <code>TransferObject</code>s obtained from DAOs. 
 * For convenience, the packages of this module follows the same organization 
 * than the packages related to <code>Entity</code>s in the <code>bgee-core</code> module.
 * <p>
 * This API provides: 
 * <ul>
 * <li>Interfaces for DAOs, that need to be implemented by the concrete implementations 
 * specific to a data source.
 * <li><code>TransferObject</code>s, that are used by the concrete implementations 
 * to communicate between the client and the data source.
 * <li>A <code>DAOFactory</code>, that: 
 *   <ul>
 *   <li>defines the methods to be implemented by the factory of the concrete implementations, 
 *   to obtain DAOs.
 *   <li>is responsible to identify which concrete factory from which concrete implementation 
 *   should be returned to the client. The client is not aware of which concrete factory 
 *   it obtains, and is only exposed to the abstract factory of this API. 
 *   This follows the <em>Abstract Factory Pattern</em>.
 *   </ul>
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
package org.bgee.model.dao.api;