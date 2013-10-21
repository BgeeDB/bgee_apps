/**
 * This package provides the classes allowing to connect to a MySQL database 
 * (see {@link org.bgee.model.dao.mysql.connector.BgeeConnection BgeeConnection}), 
 * to perform queries (see {@link org.bgee.model.dao.mysql.connector.BgeePreparedStatement 
 * BgeePreparedStatement}), and to retrieve results (see {@link 
 * org.bgee.model.dao.mysql.connector.MySQLDAOResultSet MySQLDAOResultSet}).
 * <p>
 * The entry point to this package is the {@code DAOManager}, {@link 
 * org.bgee.model.dao.mysql.connector.MySQLDAOManager MySQLDAOManager}. It allows to obtain 
 * {@code BgeeConnection}s, to obtain {@code BgeePreparedStatement}s, to obtain 
 * {@code MySQLDAOResultSet}s.
 * 
 * @author Frederic Bastian
 */
package org.bgee.model.dao.mysql.connector;