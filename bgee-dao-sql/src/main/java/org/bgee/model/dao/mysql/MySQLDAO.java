package org.bgee.model.dao.mysql;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
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
            
    /**
     * Sets the designated parameters of the given {@code BgeePreparedStatement} to the values 
     * given in {@code List} of {@code T}s.
     * <p>
     * Are currently supported: {@code String.class}, {@code Integer.class}, {@code Boolean.class}. 
     * 
     * @param stmt              A {@code BgeePreparedStatement} that is the statement 
     *                          to parameterize.
     * @param startIndex        An {@code int} that is the first index of the parameter to set.
     * @param dataList          A {@code List} of {@code T}s that is values to be used to set the 
     *                          parameters.
     * @param type              The desired type of values.
     * @throws SQLException     If parameterIndex does not correspond to a parameter marker in the 
     *                          SQL statement; if a database access error occurs or this method is 
     *                          called on a closed {@code PreparedStatement}.
     * @param <T>               A type parameter.
     */
    public static <T> void parameterizeStatement(BgeePreparedStatement stmt, int startIndex, 
            List<String> dataList, Class<T> type) throws SQLException {
        log.entry(stmt, startIndex, dataList, type);
        
        try {
            for (String data: dataList) {
                if (type.equals(String.class)) {
                    stmt.setString(startIndex, data);
                } else if (type.equals(Integer.class)) {
                    stmt.setInt(startIndex, Integer.valueOf(data));
                } else if (type.equals(Boolean.class)) {
                    stmt.setBoolean(startIndex, Boolean.valueOf(data));
                } else {
                    throw log.throwing(new IllegalArgumentException(type.getClass() +
                            " is not currently supported."));
                }
                startIndex++;
            }
        } catch (SQLException e) {
            throw log.throwing(e);
        }
        
        log.exit();
    }

    /**
     * Returns a {@code String} to be used in a parameterized query with the number of parameters 
     * equals to the given {@code size}. 
     * 
     * @param size  An {@code int} that is the number of parameters in the returned {@code String}.
     * @return      A {@code String} to be used in a parameterized query with the number of 
     *              parameters equals to the given {@code size}. 
     */
    public static String generateParameterizedQueryString(int size) {
        log.entry(size);
        
        String sql = new String();
        for (int i = 0; i < size; i++) {
            if (i > 0) { 
                sql += ", ";
            }
            sql += "?";
         }

        return log.exit(sql);
    }
}
