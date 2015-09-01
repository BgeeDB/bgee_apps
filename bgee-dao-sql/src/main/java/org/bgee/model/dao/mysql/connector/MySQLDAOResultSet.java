package org.bgee.model.dao.mysql.connector;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code DAOResultSet} implementation for MySQL. This implementation can notably 
 * ship several {@code BgeePreparedStatement}s, to iterate their results sequentially. 
 * The aim is that the caller code will not need to know whether its query 
 * to a {@code DAO} methods actually required several SQL queries under the hood. 
 * This way, the caller code will iterate the results of any number of 
 * {@code BgeePreparedStatement}s, without being aware of it. 
 * <p>
 * Note that the method {@code executeQuery} should not have been called 
 * on the {@code BgeePreparedStatement}s provided to this {@code MySQLDAOResultSet}. 
 * This is the responsibility of this {@code MySQLDAOResultSet} to do it. It will 
 * do it right away on the first {@code BgeePreparedStatement} provided, 
 * at instantiation, so that the first call to the {@code next} method could 
 * return immediately. But afterwards, if several {@code BgeePreparedStatement}s 
 * were provided, a call to the {@code next} method could generate a freeze, 
 * when this {@code MySQLDAOResultSet} gets to the end of the currently iterated 
 * {@code ResultSet}, and needs to call {@code executeQuery} on the following 
 * {@code BgeePreparedStatement}s in the list.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>   The type of {@code TransferObject} that can be obtained 
 *              from this {@code MySQLDAOResultSet}.
 */
public abstract class MySQLDAOResultSet<T extends TransferObject> implements DAOResultSet<T> {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOResultSet.class.getName());
    
    /**
     * A {@code List} of {@code BgeePreparedStatement}s that should be executed, 
     * in order. When one of them is in use, it is put in {@link #currentStatement}, 
     * to avoid calling {@code get(0)} at each iteration of {@link #next()}, as this 
     * method needs to access the {@code BgeePreparedStatement} to check its 
     * cancellation flag. When {@code executeQuery} is called, the returned 
     * {@code ResultSet} is stored in {@link #currentResultSet}.
     * @see #currentStatement
     * @see #currentResultSet
     */
    private final List<BgeePreparedStatement> statements;
    /**
     * Store the {@code BgeePreparedStatement} currently in used, meaning, 
     * it is its turned to be executed, or its returned {@code ResultSet} 
     * is currently iterated. This is because the {@code next} method needs 
     * to access it at each iteration to check its cancellation flag.
     * @see #statements
     * @see #currentResultSet
     */
    private BgeePreparedStatement currentStatement;
    /**
     * Store the {@code ResultSet} currently iterated by the {@code next} method.
     * @see #statements
     * @see #currentStatement
     */
    private ResultSet currentResultSet;
    /**
     * An {@code int} that is the number of times the method {@code next} was successfully 
     * called on {@link currentResultSet} (meaning, {@code next} returned {@code true}).
     */
    private int currentResultSetIterationCount;
    /**
     * @see #getColumnLabels()
     */
    private final Map<Integer, String> columnLabels;
    
    /**
     * An {@code int} that is the index of the parameter defining the offset argument 
     * of a LIMIT clause, in a SQL query hold by a {@code BgeePreparedStatement} 
     * used by this object.
     * @see #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean).
     */
    private final int offsetParamIndex;
    /**
     * An {@code int} that is the index of the parameter specifying the maximum number of rows 
     * to return in a LIMIT clause, in a SQL query hold by a {@code BgeePreparedStatement} 
     * used by this object.
     * @see #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean).
     */
    private final int rowCountParamIndex;
    /**
     * An {@code int} that is the maximum number of rows to use in a LIMIT clause, 
     * in a SQL query hold by a {@code BgeePreparedStatement} used by this object.
     * @see #rowCountParamIndex
     * @see #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean).
     */
    private final int rowCount;
    /**
     * An {@code int} that is the number of times a {@code BgeePreparedStatement} using 
     * a SQL query with a LIMIT clause should be executed. 
     * <p>
     * If this attribute is equal to 0, then the {@code BgeePreparedStatement} will be used 
     * for an undefined number of steps, until there are no more results returned.
     * 
     * @see #offsetParamIndex
     * @see #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean).
     */
    private final int stepCount;
    /**
     * An {@code int} that is the number of times a {@code BgeePreparedStatement} using 
     * a SQL query with a LIMIT clause has been executed so far. At each step, the offset argument 
     * of the LIMIT clause will be defined as: (currentStep) * {@link #rowCount}. So, 
     * at the first step the offset will be 0, at the second step the offset will be 
     * {@code rowCount}, etc. (at the first iteration, currentStep is equal to 0).
     * 
     * @see #stepCount
     */
    private int currentStep;
    
    /**
     * A {@code boolean} defining whether equal {@code TransferObject}s returned 
     * by different queries (whether because of the LIMIT feature, or because 
     * several statements were provided) should be filtered: when {@code true}, only one of them 
     * will be returned. This implies that all {@code TransferObject}s returned 
     * will be stored, implying potentially great memory usage.
     * <p>
     * When {@code true}, returned {@code TransferObject}s will be stored 
     * in {@link #returnedTOs};
     * 
     * @see #returnedTOs
     */
    private final boolean filterDuplicates;
    /**
     * A {@code Set} of {@code TransferObject}s storing all TOs returned, 
     * when {@link #filterDuplicates} is {@code true}, in order to detect duplicated TOs and 
     * to not return them.
     * 
     * @see #filterDuplicates;
     */
    private Set<T> returnedTOs;
    /**
     * A {@code TransferObject} that is the last one generated by a call to {@code getTO}.
     * As {@code TransferObject}s are immutable, it is safe to return a same instance 
     * even for consecutive calls to {@code getTO} for a same cursor position, 
     * and this will save memory usage. This attribute is reset to {@code null} each time 
     * the {@code next} method is called.
     * 
     * @see #getTO()
     */
    //XXX: move this mechanism to the generic DAOResultSet?
    private T lastTOGenerated;
    
    /**
     * Default constructor private, at least one {@code BgeePreparedStatement} 
     * must be provided at instantiation.
     * @see MySQLDAOResultSet(BgeePreparedStatement)
     * @see MySQLDAOResultSet(List)
     */
    @SuppressWarnings("unused")
    private MySQLDAOResultSet() {
        this((BgeePreparedStatement) null);
    }
    /**
     * Constructor providing the first {@code BgeePreparedStatement} to execute 
     * a query on. 
     * 
     * @param statement the first {@code BgeePreparedStatement} to execute 
     *                  a query on
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on {@code statement}. 
     */
    protected MySQLDAOResultSet(BgeePreparedStatement statement) {
        this(Arrays.asList(statement));
    }
    /**
     * Delegates to {@link #MySQLDAOResultSet(List, boolean)}, with the {@code boolean} 
     * argument set to {@code false}.
     * 
     * @param statements    See {@link #MySQLDAOResultSet(List, boolean)}.
     * @throws IllegalArgumentException See {@link #MySQLDAOResultSet(List, boolean)}.
     */
    protected MySQLDAOResultSet(List<BgeePreparedStatement> statements) {
        this(statements, 0, 0, 0, 0, false);
    }
    /**
     * Constructor providing some {@code BgeePreparedStatement}s to execute queries on, 
     * in order. 
     * 
     * @param statements            A {@code List} of {@code BgeePreparedStatement}s 
     *                              to execute queries on, in order.
     * @param filterDuplicates      A {@code boolean} defining whether equal {@code TransferObject}s 
     *                              returned by different queries should be filtered: 
     *                              when {@code true}, only 
     *                              one of them will be returned. This implies that all 
     *                              {@code TransferObject}s returned will be stored, implying 
     *                              potentially great memory usage.
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on any of the {@code BgeePreparedStatement}s 
     *                                  provided. 
     */
    protected MySQLDAOResultSet(List<BgeePreparedStatement> statements, boolean filterDuplicates) {
        this(statements, 0, 0, 0, 0, filterDuplicates);
    }
    /**
     * Constructor providing a {@code BgeePreparedStatement} that should be called 
     * repeatedly based on a SQL LIMIT clause. This constructor delegates to 
     * {@link #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean)}, with 
     * the last argument {@code stepCount} set to 0 (execute query until there are no more 
     * results).
     * 
     * @param statement             See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}.
     * @param offsetParamIndex      See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}.
     * @param rowCountParamIndex    See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}.
     * @param rowCount              See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}.
     * @param filterDuplicates      See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}.
     * @throws IllegalArgumentException If the parameters do not allow to correctly use 
     *                                  the LIMIT clause, or if offsetParamIndex, or 
     *                                  rowCountParamIndex, or rowCount are less than 1.
     */
    protected MySQLDAOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
            int rowCountParamIndex, int rowCount, boolean filterDuplicates) {
        this(statement, offsetParamIndex, rowCountParamIndex, rowCount, 0, filterDuplicates);
    }
    /**
     * Constructor providing a {@code BgeePreparedStatement} that should be called 
     * repeatedly based on a SQL LIMIT clause. 
     * <p>
     * The arguments of the LIMIT clause (offset, and maximum number of rows) should be 
     * specified in the query using placeholder markers ('?'), with the index of 
     * the offset parameter specified by {@code offsetParamIndex}, and the index of 
     * the parameter for max number of row specified by {@code rowCountParamIndex} 
     * (note that first parameter has an index equal to 1). 
     * <p>
     * {@code rowCount} is the value to use for the argument defining the max number of rows  
     * (using parameter at index {@code rowCountParamIndex}). 
     * <p>
     * {@code stepCount} is the number of times the {@code BgeePreparedStatement} will be executed. 
     * At each step, the offset argument of the LIMIT clause will be defined as: 
     * ('step number' -1) * {@code rowCount}. So, at the first step the offset will be 0, 
     * at the second step the offset will be {@code rowCount}, etc. If this argument is equal 
     * to 0, then the {@code BgeePreparedStatement} will be used for an undefined 
     * number of steps, until there are no more results returned.
     * <p>
     * <strong>Important: </strong> if the LIMIT clause is used in a sub-query, 
     * then there is no guarantee that there will be results from the main query 
     * at each iteration: there can be no result at a given iteration, and some results 
     * at the next iteration. For this reason, it is mandatory to provide a {code stepCount} 
     * value when it is expected that some iterations can return no results, to keep 
     * iterating until the end. It means that it is the responsibility of the caller 
     * to determine how many times the query should be iterated (for instance, based on 
     * a fist query using a 'SELECT COUNT(*) ...').
     * 
     * @param statement             A {@code BgeePreparedStatement} to execute a query on.
     * @param offsetParamIndex      An {@code int} that is the index of the parameter 
     *                              defining the offset argument of a LIMIT clause, 
     *                              in the SQL query hold by {@code statement}.
     * @param rowCountParamIndex    An {@code int} that is the index of the parameter 
     *                              specifying the maximum number of rows to return 
     *                              in a LIMIT clause, in the SQL query hold by {@code statement}.
     * @param rowCount              An {@code int} that is the maximum number of rows to use 
     *                              in a LIMIT clause, in the SQL query hold by {@code statement}.
     * @param stepCount             An {@code int} that is the number of times {@code statement} 
     *                              should be executed. If this argument is equal to 0, 
     *                              then {@code statement} will be used for an undefined 
     *                              number of steps, until there are no more results returned. 
     * @param filterDuplicates      A {@code boolean} defining whether equal {@code TransferObject}s 
     *                              returned by different queries should be filtered: 
     *                              when {@code true}, only 
     *                              one of them will be returned. This implies that all 
     *                              {@code TransferObject}s returned will be stored, implying 
     *                              potentially great memory usage.
     * @throws IllegalArgumentException If the parameters do not allow to correctly use 
     *                                  the LIMIT clause, or if offsetParamIndex, or 
     *                                  rowCountParamIndex, or rowCount are less than 1, 
     *                                  or if stepCount is less than 0, or if rowCountParamIndex 
     *                                  and offsetParamIndex are equal.
     */
    protected MySQLDAOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
            int rowCountParamIndex, int rowCount, int stepCount, boolean filterDuplicates) {
        this(Arrays.asList(statement), offsetParamIndex, rowCountParamIndex, 
                rowCount, stepCount, filterDuplicates);
        if (!this.isUsingLimitFeature() || offsetParamIndex < 1 || rowCountParamIndex < 1 || 
                rowCount < 1 || stepCount < 0 || offsetParamIndex == rowCountParamIndex) {
            throw log.throwing(new IllegalArgumentException("The parameters provided " +
            		"do not allow to correctly use the LIMIT clause. offsetParamIndex: " 
                    + offsetParamIndex + " - rowCountParamIndex: " + rowCountParamIndex + 
                    " - rowCount: " + rowCount + " - stepCount: " + stepCount));
        }
    }
    /**
     * Convenient constructor used internally to centralize instantiation process.
     * 
     * @param statements            A {@code List} of {@code BgeePreparedStatement}s 
     *                              to execute queries on, in order.
     * @param offsetParamIndex      See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}. Should be equal to 0 if the LIMIT feature 
     *                              is not used.
     * @param rowCountParamIndex    See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}. Should be equal to 0 if the LIMIT feature 
     *                              is not used.
     * @param rowCount              See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}. Should be equal to 0 if the LIMIT feature 
     *                              is not used.
     * @param stepCount             See {@link #MySQLDAOResultSet(BgeePreparedStatement, int, 
     *                              int, int, int, boolean)}. Should be equal to 0 if the LIMIT feature 
     *                              is not used.
     * @param filterDuplicates      A {@code boolean} defining whether equal {@code TransferObject}s 
     *                              returned by different queries should be filtered: 
     *                              when {@code true}, only 
     *                              one of them will be returned. This implies that all 
     *                              {@code TransferObject}s returned will be stored, implying 
     *                              potentially great memory usage.
     */
    private MySQLDAOResultSet(List<BgeePreparedStatement> statements, int offsetParamIndex, 
            int rowCountParamIndex, int rowCount, int stepCount, boolean filterDuplicates) {
        log.entry(statements, offsetParamIndex, rowCountParamIndex, 
                rowCount, stepCount, filterDuplicates);
        
        if (statements.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one PreparedStatement " +
                    "must be provided"));
        }
        
        this.offsetParamIndex = offsetParamIndex;
        this.rowCountParamIndex = rowCountParamIndex;
        this.rowCount = rowCount;
        this.stepCount = stepCount;
        this.currentStep = 0;
        this.filterDuplicates = filterDuplicates;
        this.returnedTOs = new HashSet<T>();
        this.lastTOGenerated = null;
        
        if (statements.size() > 1 && this.isUsingLimitFeature()) {
            throw log.throwing(new IllegalArgumentException("The LIMIT feature is supported " +
                    "for only one PreparedStatement"));
        }
        
        for (BgeePreparedStatement stmt: statements) {
            if (stmt.isExecuted()) {
                throw log.throwing(new IllegalArgumentException("A BgeePreparedStatement " +
                        "should not have been executed before being provided " +
                        "to the MySQLDAOResultSet"));
            }
        }
        
        this.statements = new ArrayList<BgeePreparedStatement>(statements);
        this.columnLabels = new HashMap<Integer, String>();
        this.currentResultSet = null;
        this.currentResultSetIterationCount = 0;
        this.currentStatement = null;
        
        this.executeNextStatementQuery();
        log.exit();
    }

    @Override
    public boolean next() throws DAOException, QueryInterruptedException {
        log.entry();
        this.lastTOGenerated = null;
        //If currentResultSet is null, it means that there are no more 
        //BgeePreparedStatement to obtain results from. 
        if (this.currentResultSet == null) {
            return log.exit(false);
        }
        this.checkCurrentStatementCanceled();
        
        try {
            //if we get at the end of the current ResultSet, try to execute the next 
            //BgeePreparedStatement in order
            if (!this.currentResultSet.next()) {
                //this method will notably close the current resultset before moving to the next query. 
                this.executeNextStatementQuery();
                
                //we use recursivity here: maybe the next statement did not return 
                //any result, but another one  afterwards might. This should be invisible 
                //to the user.
                return log.exit(this.next());
            }
            //otherwise, keep on iterating the current ResultSet. 
            //we count this iteration even if it corresponds to a duplicated TO, 
            //as we need to know whether the database can potentially have other results.
            this.currentResultSetIterationCount++;
            //check whether we need to filter duplicated TOs
            if (this.isFilterDuplicates()) {
                T to = this.getTO();
                if (to != null && !this.returnedTOs.add(to)) {
                    //duplicate TO, we do not want to return it, use recursivity to move 
                    //to next row
                    return log.exit(this.next());
                } 
            }
            return log.exit(true);
            
        } catch (SQLException e) {
            this.close();
            throw log.throwing(new DAOException(e));
        } catch (DAOException e) {
            this.close();
            throw log.throwing(e);
        } catch (QueryInterruptedException e) {
            this.close();
            throw log.throwing(e);
        }
    }
    
    @Override
    public T getTO() throws DAOException {
        log.entry();
        //as TransferObjects should be immutable, it is safe to return a same instance 
        //even if getTO is called several times. This will save memory usage.
        //this attribute is reset to null each time the next method is called.
        if (this.lastTOGenerated == null) {
            if (this.currentResultSet == null) {
                throw log.throwing(new IllegalStateException("Cannot retrieve a TransferObject " +
                		"after all results have been iterated"));
            }
            
            try {
                this.lastTOGenerated = this.getNewTO();
            } catch (DAOException e) {
                this.close();
                throw log.throwing(e);
            }
        }
        return log.exit(this.lastTOGenerated);
    }
    /**
     * Returns the result corresponding to the current cursor position of this 
     * {@code DAOResultSet} (see {@link #next()}) as a {@code TransferObject} {@code T}, 
     * newly instantiated. The difference with {@link #getTO} is that {@code getTO} 
     * might sometimes uses a "cached" {@code TransferObject}, while this method would always 
     * instantiate a new one.
     * <p>
     * If the cursor is not positioned on a result when this method is called, 
     * it throws a {@code DAOException}.
     * 
     * @return  The {@code TransferObject} {@code T} corresponding to the result 
     *          at the current cursor position of this {@code DAOResultSet}.
     * @throws DAOException                 If an error occurs while retrieving the result.
     * @throws UnrecognizedColumnException  If a column name in a result set is not recognized.
     */
    protected abstract T getNewTO() throws DAOException, UnrecognizedColumnException;
    
    @Override
    public List<T> getAllTOs() throws DAOException {
        log.entry();
        List<T> allTOs = new ArrayList<T>();
        try {
            while (this.next()) {
                allTOs.add(this.getTO());
            }
            return log.exit(allTOs);
        } finally {
            this.close();
        }
    }

    /**
     * Returns the {@code ResultSet} corresponding to the current cursor position of this 
     * {@code DAOResultSet}. The {@code ResultSet} is null, this simply means that we have no 
     * more {@code ResultSet}.
     * 
     * @return 	The current {@code ResultSet} corresponding to the current cursor position of 
     * 			this {@code DAOResultSet}.
     */
    protected ResultSet getCurrentResultSet() {
    	return this.currentResultSet;
    }
    /**
     * Returns an unmodifiable {@code Map} associating column indexes to column labels. 
     * Keys are {@code Integer}s representing column indexes, corresponding values 
     * are {@code String}s being the column labels, from the metadata obtained 
     * from the current {@code ResultSet} (see {@link #getCurrentResultSet()}). 
     * <p>
     * This {@code Map} is populated by calling {@code getColumnLabel} 
     * on the {@code ResultSetMetaData} object obtained from the current {@code ResultSet}. 
     * This information is loaded when the current {@code ResultSet} is set 
     * (see {@link #executeNextStatementQuery()}).
     * <p>
     * Note that these are the labels that are stored, not the column names. From the JDBC 
     * javadoc, they are "the designated column's suggested title for use 
     * in printouts and displays. The suggested title is usually specified by the SQL 
     * {@code AS} clause. If a SQL {@code AS} is not specified, the label will be 
     * the same as the column name".
     * 
     * @return  An unmodifiable {@code Map} where keys are {@code Integer}s and values 
     *          are {@code String}s, providing the association from column indexes 
     *          to column labels. 
     */
    protected Map<Integer, String> getColumnLabels() {
        return Collections.unmodifiableMap(this.columnLabels);
    }
    
    @Override
    public void close() throws DAOException {
        log.entry();
        this.closeCurrentResultSet();
        this.closeCurrentPreparedStatement();
        for (BgeePreparedStatement stmt: this.statements) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.catching(e);
                throw log.throwing(new DAOException(e));
            }
        }
        this.statements.clear();
        this.columnLabels.clear();
        this.returnedTOs.clear();
        this.lastTOGenerated = null;
        log.exit();
    }
    
    /**
     * Execute the query of the first {@code BgeePreparedStatement} available 
     * in {@link #statements}. This method obtains and removes from {@code statements} 
     * the element with index 0, put it in {@link #currentStatement}, calls 
     * {@code executeQuery} on it, and stores the returned {@code ResultSet} 
     * into {@link #currentResultSet}. 
     * <p>
     * This method is always called at least at instantiation, by the constructor. 
     * It will then be called additionally if additional {@code BgeePreparedStatement}s 
     * were provided, when the current {@code ResultSet} would have been completely 
     * iterated (its {@code next} method returns {@code false}).
     * 
     * @throws DAOException If a {@code SQLException} occurred when calling 
     *                      {@code executeQuery}.
     */
    private void executeNextStatementQuery() throws DAOException, QueryInterruptedException {
        log.entry();
        try {
            if (this.isUsingLimitFeature() && 
                    this.stepCount != 0 && this.currentStep > this.stepCount) {
                //if we use the LIMIT feature and we have defined a maximum number 
                //of iterations, and we exceed it, we did something wrong ^^
                throw log.throwing(new IllegalStateException("The PreparedStatement " +
                		"using a LIMIT clause should not have been executed more than " +
                		this.stepCount + " times, was executed " + this.currentStep + " times."));
            }
            //store number of rows retrieved from the current ResultSet, calling 
            //closeCurrentResultSet will reinit it.
            int resultSetIterationCount = this.currentResultSetIterationCount;
            this.closeCurrentResultSet();
            
            //we try to move to the next statement either because we were not using the LIMIT feature, 
            //or, if we were using the LIMIT feature, because we retrieved all results 
            //for this statement (either because we reached stepCount, or because 
            //stepCount was not defined and there were no more results to retrieve); 
            //we do not check only whether number of results 
            //retrieved was less than rowCount, because if the LIMIT is used in a sub-query, 
            //the number of results returned can be different from rowCount); sometimes, 
            //there can even be no results returned for an iteration, then results again 
            //at the next iteration; the only way to deal with such cases is to provide 
            //a stepCount.
            //Also, at the first call following instantiation, resultSetIterationCount 
            //and currentStep will be equal to 0, so we will get the first statement.
            if (!this.isUsingLimitFeature() || 
                    //if it is the first iteration, acquire the next statement to iterate
                    this.currentStep == 0 || 
                    //or if we have iterated the current statement the requested number of times
                    this.currentStep == this.stepCount || 
                    //or if we did not configure the number of iterations, and there were 
                    //no results at the previous one.
                    (this.stepCount == 0 && resultSetIterationCount == 0)) {
                log.trace("Try to move to next statement");
                //currentStep is set to 0 when calling closeCurrentPreparedStatement
                this.closeCurrentPreparedStatement();
                this.currentStatement = this.statements.remove(0);
            } 
            if (this.isUsingLimitFeature()) {
                //use the LIMIT feature
                log.trace("Next step for query using a LIMIT clause");
                //note that at first iteration, currentStep is equal to 0
                this.currentStatement.setInt(this.offsetParamIndex, 
                        this.currentStep * this.rowCount);
                this.currentStatement.setInt(this.rowCountParamIndex, this.rowCount);
                this.currentStep++;
                
            } 
            
            this.checkCurrentStatementCanceled();
            this.currentResultSet = this.currentStatement.executeQuery();
            //store currentResultSet column labels
            this.columnLabels.clear();
            ResultSetMetaData metaData = this.currentResultSet.getMetaData();
            for (int column = 1; column <= metaData.getColumnCount(); column++) {
                this.columnLabels.put(column, metaData.getColumnLabel(column));
            }
        } catch (IndexOutOfBoundsException e) {
            //this simply means that we have no more BgeePreparedStatement to iterate.
            log.trace("No more statements to execute");
            this.currentResultSet = null;
            this.currentResultSetIterationCount = 0;
            this.currentStatement = null;
            this.columnLabels.clear();
            this.returnedTOs.clear();
            this.lastTOGenerated = null;
            log.catching(Level.TRACE, e);
        } catch (SQLException e) {
            //here, this is bad ;)
            this.close();
            log.catching(e);
            throw log.throwing(new DAOException(e));
        }
        log.exit();
    }
    
    /**
     * Closes {@link #currentResultSet} and sets it to {@code null}.
     * @throws  DAOException if an error occurred while closing the {@code ResultSet}.
     */
    private void closeCurrentResultSet() throws DAOException{
        log.entry();
        try {
            if (this.currentResultSet != null && !this.currentResultSet.isClosed()) {
                this.currentResultSet.close();
            }
        } catch (SQLException e) {
            //to avoid an infinite loop when calling close, which calls closeCurrent
            this.currentResultSet = null;
            this.currentResultSetIterationCount = 0;
            //this is to close the remaining BgeePreparedStatements
            this.close();
            log.catching(e);
            throw log.throwing(new DAOException(e));
        }
        this.currentResultSet = null;
        this.currentResultSetIterationCount = 0;
        log.exit();
    }
    /**
     * Closes {@link #currentStatement} and sets it to {@code null}.
     * @throws  DAOException if an error occurred while closing the {@code BgeePreparedStatement}.
     */
    private void closeCurrentPreparedStatement() throws DAOException{
        log.entry();
        //closing the statement is supposed to close the ResultSet, but we are 
        //never too careful
        try {
            if (this.currentStatement != null) {
                this.currentStatement.close();
            }
        } catch (SQLException e) {
            //to avoid an infinite loop when calling close, which calls closeCurrent
            this.currentStatement = null;
            this.currentStep = 0;
            //this is to close the remaining BgeePreparedStatements
            this.close();
            log.catching(e);
            throw log.throwing(new DAOException(e));
        }
        this.currentStatement = null;
        this.currentStep = 0;
        log.exit();
    }
    
    /**
     * Determines whether the {@code BgeePreparedStatement}s executed by 
     * this {@code MySQLDAOResutSet} use the LIMIT feature (see
     * {@link #MySQLDAOResultSet(BgeePreparedStatement, int, int, int, int, boolean)}).
     * 
     * @return  {@code true} if the LIMIT feature is used.
     */
    public boolean isUsingLimitFeature() {
        log.entry();
        if (this.offsetParamIndex == 0) {
            return log.exit(false);
        }
        if (this.rowCountParamIndex == 0) {
            return log.exit(false);
        }
        if (this.rowCount == 0) {
            return log.exit(false);
        }
        return log.exit(true);
    }
    
    /**
     * Checks whether {@link #currentStatement} was requested to be canceled 
     * ({@link BgeePreparedStatement#isCanceled()} returns {@code true}). If it is 
     * the case, it calls the method {@link #close()} and throws a 
     * {@code QueryInterruptedException}.
     * 
     * @throws QueryInterruptedException    if {@link #currentStatement} was canceled.
     */
    private void checkCurrentStatementCanceled() throws QueryInterruptedException {
        log.entry();
        if (this.currentStatement.isCanceled()) {
            //to close remaining BgeePreparedStatements
            this.close();
            throw log.throwing(new QueryInterruptedException());
        }
        log.exit();
    }
    
//    /**
//     * Add {@code stmt} at the tail of the {@code List} of {@code BgeePreparedStatement} 
//     * that should be executed by this {@code MysqlDAOResultSet}.
//     * 
//     * @param stmt  A {@code BgeePreparedStatement} to add to this {@code MysqlDAOResultSet}.
//     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
//     *                                  on {@code stmt}. 
//     */
//    protected void addStatement(BgeePreparedStatement stmt) throws IllegalArgumentException {
//        this.addAllStatements(Arrays.asList(stmt));
//    }
//    
//    /**
//     * Add {@code statements} at the tail of the {@code List} of 
//     * {@code BgeePreparedStatement} that should be executed by this 
//     * {@code MysqlDAOResultSet}, in order.
//     * 
//     * @param statements  A {@code List} of {@code BgeePreparedStatement}s to be added 
//     *              to the tail of the {@code List} held by this {@code MysqlDAOResultSet}, 
//     *              to be executed in order.
//     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
//     *                                  on any of the {@code BgeePreparedStatement}s 
//     *                                  provided. 
//     */
//    protected void addAllStatements(List<BgeePreparedStatement> statements) 
//            throws IllegalArgumentException {
//        for (BgeePreparedStatement stmt: statements) {
//            if (stmt.isExecuted()) {
//                throw log.throwing(new IllegalArgumentException("A BgeePreparedStatement " +
//                        "should not have been executed before being provided " +
//                        "to the MySQLDAOResultSet"));
//            }
//        }
//        this.statements.addAll(statements);
//    }
    
    /**
     * @return  an {@code int} that is the number of {@code BgeePreparedStatement}s 
     *          currently held by this {@code MySQLDAOResultSet}.
     */
    protected int getStatementCount() {
        return this.statements.size();
    }
    /**
     * @return  A {@code boolean} defining whether equal {@code TransferObject}s returned 
     *          by different queries (whether because of the LIMIT feature, or because 
     *          several statements were provided) should be filtered: when {@code true}, 
     *          only one of them will be returned. This implies that all 
     *          {@code TransferObject}s returned will be stored, implying potentially 
     *          great memory usage.
     */
    public boolean isFilterDuplicates() {
        return this.filterDuplicates;
    }
    
}
