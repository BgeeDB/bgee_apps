package org.bgee.model.dao.mysql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

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
    public Set<T> getAttributes() {
        log.entry();
        return log.exit(new HashSet<T>(attributes));
    }
    
    
    //*************************************************************
    // HELPER METHODS TO GENERATE SELECT CLAUSES FROM ATTRIBUTES, 
    // OR TO MAP COLUMN NAMES OF RESULT SETS TO ATTRIBUTES
    //*************************************************************
    
    /**
     * Get the {@code Attribute} corresponding to the column name of a result set. 
     * The mapping is retrieved from {@code colNamesToAttributes}. This helper method 
     * should be used used in methods implementing 
     * {@link org.bgee.model.dao.mysql.connector.MySQLDAOResultSet#getNewTO() MySQLDAOResultSet#getNewTO()}, 
     * to determine how to populate a {@code TransferObject} from a row in a result set. 
     * This is why this method directly throws an {@code UnrecognizedColumnException} 
     * if no {@code Attribute} corresponding to {@code colName} could be found: this is 
     * the expected behavior of the {@code getNewTO} methods.
     * <p>
     * See {@link #getSelectExprFromAttribute(T, Map)} for the opposite helper method, 
     * that can be used to generate the 'select_expr's in the SELECT clause of a query. 
     * Note that column names of a result set can correspond exactly to the 'select_expr's used 
     * in the query, or they can differ if aliases were used. In the latter case, this method 
     * and the method {@code getSelectExprFromAttribute} should use different mappings: 
     * this method should be provided with a mapping between aliases and {@code Attribute}s, 
     * {@code getSelectExprFromAttribute} with a mapping between 'select_expr's and {@code Attribute}s.
     * In the former case, they can be provided with the exact same mapping.
     * 
     * @param colName               A {@code String} representing a column name in a result set.
     * @param colNamesToAttributes  A {@code Map} where keys are {@code String}s corresponding to 
     *                              supported column names, associated to their corresponding 
     *                              {@code Attribute} as values.
     * @return                      An {@code Attribute} {@code T} corresponding to {@code colName}.
     * @throws UnrecognizedColumnException  If {@code colName} does not correspond to 
     *                                      any {@code Attribute} in {@code colNamesToAttributes}.
     * @see #getSelectExprFromAttribute(T, Map)
     */
    protected T getAttributeFromColName(String colName, Map<String, T> colNamesToAttributes) 
            throws UnrecognizedColumnException {
        log.entry(colName, colNamesToAttributes);
        T attribute = colNamesToAttributes.get(colName);
        if (attribute == null) {
            throw log.throwing(new UnrecognizedColumnException(colName));
        } 
        return log.exit(attribute);
    }
    /**
     * Get the 'select_expr' of a SELECT clause corresponding to the {@code Attribute} {@code attr}.
     * The mapping is retrieved from {@code selectExprsToAttributes}. This helper method  
     * is most likely used when writing a SQL query. See also the simple helper method 
     * {@link #generateSelectClause(String, Map)}.
     * <p>
     * See {@link #getAttributeFromColName(String, Map)} for the opposite helper method, 
     * that can be used to retrieve the {@code Attribute} corresponding to the column name of a result set. 
     * Note that column names of a result set can correspond exactly to the 'select_expr's used 
     * in the query, or they can differ if aliases were used. In the latter case, this method 
     * and the method {@code getAttributeFromColName} should use different mappings: 
     * this method should be provided with a mapping between 'select_expr's and {@code Attribute}s, 
     * {@code getAttributeFromColName} with a mapping between aliases and {@code Attribute}s.
     * In the former case, they can be provided with the exact same mapping.
     * 
     * @param attr                      An {@code Attribute} {@code T} to be mapped to a 'select_expr'.
     * @param selectExprsToAttributes   A {@code Map} where keys are {@code String}s corresponding to 
     *                                  'select_expr's, associated to their corresponding 
     *                                  {@code Attribute} as values. This {@code Map} does not have  
     *                                  {@code Attribute}s as keys for coherence with the method 
     *                                  {@code getAttributeFromColName}.
     * @return                          A {@code String} that is a 'select_expr' corresponding to {@code attr}.
     * @throws IllegalArgumentException If {@code attr} does not correspond to any 'select_expr' 
     *                                  in {@code selectExprsToAttributes}, or if several 'select_expr's 
     *                                  are mapped to a same {@code Attribute}.
     * @see #getAttributeFromColName(String, Map)
     * @see #reverseColNameMap(Map)
     * @see #generateSelectClause(String, Map)
     */
    protected String getSelectExprFromAttribute(T attr, Map<String, T> selectExprsToAttributes) 
            throws IllegalArgumentException {
        log.entry(attr, selectExprsToAttributes);
        
        //we reverse the provided Map, because anyway we want to check all Entries 
        //to make sure there is not several 'select_expr's mapped to a same Attribute
        Map<T, String> reverseMap = new HashMap<T, String>();
        try {
            reverseMap = selectExprsToAttributes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        } catch (IllegalStateException e) {
            //wrap the IllegalStateException thrown by Collectors.toMap into an IllegalArgumentException
            throw log.throwing(new IllegalArgumentException(
                    "An Attribute is mapped to several 'select_expr's", e));
        }
        
        String selectExpr = reverseMap.get(attr);
        if (StringUtils.isBlank(selectExpr)) {
            throw log.throwing(new IllegalArgumentException(
                    "Attribute not mapped to any valid 'select_expr': " + attr));
        } 
        return log.exit(selectExpr);
    }
    /**
     * Helper method to generate the SELECT clause of a query, from the {@code Attribute}s 
     * returned by {@link #getAttributes()}, using the method 
     * {@link #getSelectExprFromAttribute(T, Map)}. This method helps only in simple cases, 
     * more complex statements should be hand-written (for instance, when {@code Attribute}s 
     * correspond to columns in different tables, or to a sub-query). 
     * 
     * @param tableName                 A {@code String} that is the name of the table 
     *                                  to retrieve data from, or its alias defined in the query.
     * @param selectExprsToAttributes   A {@code Map} where keys are {@code String}s corresponding to 
     *                                  'select_expr's, associated to their corresponding 
     *                                  {@code Attribute} as values. This {@code Map} does not have  
     *                                  {@code Attribute}s as keys for coherence with the method 
     *                                  {@link getAttributeFromColName(T, Map)}.
     * @param distinct                  A {@code boolean} defining whether the DISTINCT keyword 
     *                                  is needed in the SELECT clause.
     * @return                          A {@code String} that is the generated SELECT clause.
     * @throws IllegalArgumentException If {@code selectExprsToAttributes} is missing a key corresponding 
     *                                  to an {@code Attribute} returned by {@link #getAttributes()}, 
     *                                  or if several 'select_expr's are mapped to a same {@code Attribute}.
     * @see #getAttributes()
     * @see #getSelectExprFromAttribute(T, Map)
     * @see #reverseColNameMap(Map)
     */
    protected String generateSelectClause(String tableName, Map<String, T> selectExprsToAttributes, 
            boolean distinct) throws IllegalArgumentException {
        log.entry(tableName, selectExprsToAttributes);
        
        StringBuilder sb = new StringBuilder("SELECT ");
        if (distinct) {
            sb.append("DISTINCT ");
        }
        //any attribute requested
        if (this.getAttributes() == null || this.getAttributes().isEmpty() || 
                //if all attributes were requested
                this.getAttributes().containsAll(Arrays.asList(
                        this.getAttributes().iterator().next().getClass().getEnumConstants()))) {
            
            sb.append(tableName).append(".* ");
            
        } else {
            //sort the Attributes to improve chances of cache hit
            List<T> sortedAttributes = new ArrayList<T>(this.getAttributes());
            sortedAttributes.sort(Comparator.comparing(Enum::ordinal));
            
            int attrCount = 0;
            for (T attribute : sortedAttributes) {
                if (attrCount++ > 0) {
                    sb.append(", ");
                }
                sb.append(tableName).append(".")
                    .append(getSelectExprFromAttribute(attribute, selectExprsToAttributes));
            }
            sb.append(" ");
        }

        return log.exit(sb.toString());
    }
    
    
    /**
     * Convert a {@code Collection} of {@code String}s into a {@code List} of {@code Integer}s, 
     * order in the returned {@code List} is the natural ordering of {@code Integer}s.
     * Each element will be converted into an {@code int}, and a {@code NumberFormatException} 
     * is thrown if an element does not contain a parsable integer.
     * 
     * @param strings   A {@code Collection} of {@code String}s to be converted 
     *                  into an ordered {@code List} of {@code Integer}s.
     * @return          A {@code List} of {@code Integer}s corresponding to {@code strings}, 
     *                  with the natural ordering of {@code Integer}s.
     * @throws NumberFormatException    if an element of {@code strings} is not parsable 
     *                                  into an {@code Integer}.
     */
    protected static List<Integer> convertToOrderedIntList(Collection<String> strings) 
            throws NumberFormatException {
        log.entry(strings);
        if (strings == null) {
            return log.exit(null);
        }
        List<Integer> intList = new ArrayList<Integer>(strings.size());
        for (String val: strings) {
            intList.add(Integer.parseInt(val));
        }
        Collections.sort(intList);
        return log.exit(intList);
    }

    /**
     * Generate a select statement using the table name in the {@code FROM} clause.
     * @param tableName The table name as a {@code String}
     * @param columnToAttributesMap A map from column name (as {@code String}) to {@code Attributes}
     * @param distinct A {@code boolean} defining whether the DISTINCT keyword is needed in the SELECT clause.
     * @return The generated statement as a {@code String}
     */
    protected  String generateSelectAllStatement(String tableName,
                                                 Map<String, T> columnToAttributesMap, boolean distinct) {
        log.entry();
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(tableName, columnToAttributesMap, distinct));
        sb.append(" FROM " + tableName);
        return log.exit(sb.toString());
    }
}
