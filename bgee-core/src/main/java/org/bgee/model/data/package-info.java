/**
 * This package is used to access data sources. This package is completely independent 
 * from other packages (it never uses classes from other packages). 
 * It has the same packages organization than the "entity" packages 
 * (see {@link org.bgee.model} for a description of the "entity" packages).
 * <p>
 * The principle of this package is that, for each "entity" class, it exists a corresponding 
 * DAO interface, that needs to be implemented for a specific data source 
 * (for instance, MySQL). And for each "entity" class, it exists a corresponding 
 * <code>TransferObject</code>, that allows to encapsulate all information about an entity, 
 * retrieved from a data source. 
 * <code>TransferObject</code>s are returned by the methods declared in the DAO interfaces. 
 * They are then used by the "entities" factories to instantiate and populate classes. 
 * <p>
 * For instance, for the {@link org.bgee.model.gene.GeneFactory GeneFactory} 
 * to provide {@link org.bgee.model.gene.Gene Gene}s, it calls a DAO implementing 
 * the {@link org.bgee.model.data.common.gene.GeneDAO GeneDAO} interface 
 * (for instance, the {@link org.bgee.model.data.sql.mysql.gene.MySQLGeneDAO MySQLGeneDAO}), 
 * that returns {@link org.bgee.model.data.common.gene.GeneTO GeneTO}s.
 * <p>
 * The DAO interfaces and the <code>TransferObject</code>s are located in the package 
 * {@link org.bgee.model.data.common}. Each other sub-package of <code>org.bgee.model.data</code> 
 * is dedicated to a specific data source, and contain the DAOs implementing the interfaces 
 * defined in <code>org.bgee.model.data.common</code> for that specific data source. 
 * For instance, the {@link org.bgee.model.data.sql} 
 * package is dedicated to querying SQL databases, and inside it, the DAOs present 
 * in the package {@link org.bgee.model.data.sql.mysql} are dedicated to querying a MySQL database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
package org.bgee.model.data;