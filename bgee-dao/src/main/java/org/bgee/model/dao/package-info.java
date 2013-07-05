/**
 * This package is used to access data sources. It is completely independent 
 * from other packages and modules. It implements the 
 * {@link http://www.oracle.com/technetwork/java/dataaccessobject-138824.html 
 * J2EE Data Access Object Patterns}. It notably provides DAOs, that returns 
 * or obtains data by means of <code>TransferObject</code>s. 
 * A user of this <code>bgee-dao</code> module 
 * is notably the <code>bgee-core</code> module, that uses it to instantiate 
 * <code>Entity</code>s, using the <code>TransferObject</code>s obtained from the DAOs. 
 * For convenience, the packages of this module follows the same organization 
 * than the packages related to <code>Entity</code>s in the <code>bgee-core</code> module.
 * <p>
 * 
 * <p>
 * The DAO interfaces and the <code>TransferObject</code>s are located in the package 
 * {@link org.bgee.model.dao.common}. Each other sub-package of <code>org.bgee.model.dao</code> 
 * is dedicated to a specific data source, and contain the DAOs implementing the interfaces 
 * defined in <code>org.bgee.model.dao.common</code> for that specific data source. 
 * For instance, the {@link org.bgee.model.dao.sql} 
 * package is dedicated to querying SQL databases, and inside it, the DAOs present 
 * in the package {@link org.bgee.model.dao.sql.mysql} are dedicated to querying a MySQL database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
package org.bgee.model.dao;