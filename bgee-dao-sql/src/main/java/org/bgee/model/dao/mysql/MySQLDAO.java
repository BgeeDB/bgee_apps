package org.bgee.model.dao.mysql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * Parent class of all MySQL DAOs of this module.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 */
public abstract class MySQLDAO<T extends Enum<?> & DAO.Attribute> implements DAO<T> {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAO.class.getName());
    /**
     * A {@code Set} of {@code DAO.Attribute}s specifying the attributes to retrieve 
     * from the data source in order to build {@code TransferObject}s associated to 
     * this {@code DAO}.
     */
    private final Set<T> attributes;
    
    /**
     * The {@code MySQLDAOManager} used by this {@code MySQLDAO} to obtain 
     * {@code BgeeConnection}s.
     */
    private final MySQLDAOManager manager;
    
    /**
     * Default constructor private, should not be used, a {@code MySQLDAOManager} 
     * should always be provided, see {@link #MySQLDAO(MySQLDAOManager)}.
     */
    @SuppressWarnings("unused")
    private MySQLDAO() {
        this(null);
    }
    /**
     * Default constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        //attributes could be an EnumSet, but we would need to provide the class 
        //of T to the constructor in order to get the type...
        this.attributes = new HashSet<T>();
        if (manager == null) {
            throw log.throwing(new IllegalArgumentException("The MySQLDAOManager " +
            		"cannot be null"));
        }
        this.manager = manager;
    }
    
    /**
     * @return  The {@code MySQLDAOManager} used by this {@code MySQLDAO} to obtain 
     *          {@code BgeeConnection}s.
     */
    protected MySQLDAOManager getManager() {
        return this.manager;
    }

    @Override
    public void setAttributesToGet(Collection<T> attributes) {
        log.entry(attributes);
        this.clearAttributesToGet();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
        log.exit();
    }
    /*
     * (non-Javadoc)
     * suppress warning because this method is robust to heap pollution, it only depends 
     * on the fact that the array will contain {@code T} elements, not on the fact 
     * that it is an array of {@code T}; we can add the @SafeVarargs annotation. 
     * See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
     */
    @SafeVarargs
    @Override
    public final void setAttributesToGet(T... attributes) {
        log.entry((Object[]) attributes);
        Set<T> newAttributes = new HashSet<T>();
        for (int i = 0; i < attributes.length; i++) {
            newAttributes.add(attributes[i]);
        }
        this.setAttributesToGet(newAttributes);
        log.exit();
    }
    @Override
    public void clearAttributesToGet() {
        log.entry();
        this.attributes.clear();
        log.exit();
    }
    @Override
    public Collection<T> getAttributesToGet() {
        log.entry();
        Set<T> attributeCopy = new HashSet<T>(attributes) ;
        return log.exit(attributeCopy);
    }
    
//  /**
//  * Returns the select expression corresponding to the provided {@code Attribute} 
//  * (in most cases, this corresponds to the column name of a MySQL table). 
//  * This method allows to build the SQL statements used by this {@code MySQLDAO}. 
//  * If no select expression corresponds to {@code attribute}, 
//  * an {@code IllegalArgumentException} should be thrown.
//  * 
//  * @param attribute     An {@code Attribute} which we want the column associated to, 
//  *                      in the MySQL schema.  
//  * @return              A {@code String} that is the column name corresponding to 
//  *                      {@code attribute}.
//  * @throw IllegalArgumentException  If no select expression is associated to 
//  *                                  {@code attribute}.
//  */
// protected String getSelectExpression(T attribute) {
//     if (attribute instanceof ...) {
//         
//     }
// }
// 
// /**
//  * Returns the name of the table associated to this {@code MySQLDAO} in the MySQL schema.
//  * 
//  * @return  A {@code String} that is the name 
//  */
// protected  abstract String getTableName();
}
