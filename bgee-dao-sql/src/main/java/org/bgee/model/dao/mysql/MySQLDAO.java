package org.bgee.model.dao.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
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
     * An {@code int} that is the maximum number of rows that can be inserted in a 
     * single INSERT or UPDATE statements.
     */
    protected final static int MAX_UPDATE_COUNT = 10000;
    
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
    public void setAttributes(Collection<T> attributes) {
        log.entry(attributes);
        this.clearAttributes();
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
    public final void setAttributes(T... attributes) {
        log.entry((Object[]) attributes);
        Set<T> newAttributes = new HashSet<T>();
        for (int i = 0; i < attributes.length; i++) {
            newAttributes.add(attributes[i]);
        }
        this.setAttributes(newAttributes);
        log.exit();
    }
    
    @Override
    public void clearAttributes() {
        log.entry();
        this.attributes.clear();
        log.exit();
    }
    
    @Override
    public Collection<T> getAttributes() {
        log.entry();
        Set<T> attributeCopy = new HashSet<T>(attributes) ;
        return log.exit(attributeCopy);
    }
    
    @Override
    public <O extends TransferObject> List<O> getAllTOs(DAOResultSet<O> resultSet) 
        throws DAOException {
        log.entry(resultSet);
        List<O> allTOs = new ArrayList<O>();
        while (resultSet.next()) {
            allTOs.add(resultSet.getTO());
        }
        resultSet.close();
        return log.exit(allTOs);
    }
    
    /**
     * Create a {@code String} composed with all {@code String}s of a {@code Set} separated 
     * by the given separator.
     * <p>
     * That methods is useful for passing a {@code Set} of {@code String} (for instance, IDs) 
     * to a store procedure that does not accept {@code Collection} or array.
     * 
     * @param set       A {@code Set} of {@code String}s that must be put into a single 
     *                  {@code String}.
     * @param separator A {@code char} that is the separator to use.
     * @return          A {@code String} composed with all {@code String}s of a {@code Set} 
     *                  separated by the given separator. If {@code Set} is null or empty, 
     *                  returns an empty {@code String}.
     */
    public String createStringFromSet(Set<String> set, char separator) {
        log.entry(set);
        if (set == null || set.size() ==0) {
            return log.exit("");
        }
        StringBuilder myString = new StringBuilder();
        Iterator<String> i = set.iterator();
        boolean isFirst = true;
        while(i.hasNext() ) {
            if (!isFirst && set.size() > 1) {
                myString.append(separator);
            }
            myString.append(i.next());
            isFirst = false;
        }
        return log.exit(myString.toString());
    }
}
