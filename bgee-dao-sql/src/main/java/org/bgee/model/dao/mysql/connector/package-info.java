/**
 * This package provides the classes allowing to connect to a MySQL database 
 * (see {@link BgeeConnection}), to perform queries (see {@link BgeePreparedStatement}), 
 * and to retrieve results (see {@link MySQLDAOResultSet}).
 * <p>
 * The entry point to this package is the {@code DAOManager}, {@link MySQLDAOManager}. 
 * It allows to obtain {@code BgeeConnection}s, to obtain {@code BgeePreparedStatement}s, 
 * to obtain {@code MySQLDAOResultSet}s.
 * 
 * @author Frederic Bastian
 */
package org.bgee.model.dao.mysql.connector;