package org.bgee.model.dao.mysql.connector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;

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
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class MySQLDAOResultSet implements DAOResultSet {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOResultSet.class.getName());
    
    /**
     * The {@code List} of {@code BgeePreparedStatement}s that should be executed, 
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
     * a query on. Note that additional {@code BgeePreparedStatement}s can be provided 
     * afterwards by calling {@link #addStatement(BgeePreparedStatement)} or 
     * {@link #addAllStatements(List)}.
     * 
     * @param statement the first {@code BgeePreparedStatement} to execute 
     *                  a query on
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on {@code statement}. 
     */
    public MySQLDAOResultSet(BgeePreparedStatement statement) {
        this(Arrays.asList(statement));
    }
    /**
     * Constructor providing some {@code BgeePreparedStatement}s to execute queries on, 
     * in order. Note that additional {@code BgeePreparedStatement}s can be provided 
     * afterwards by calling {@link #addStatement(BgeePreparedStatement)} or 
     * {@link #addAllStatements(List)}.
     * 
     * @param statements    A {@code List} of {@code BgeePreparedStatement}s 
     *                      to execute queries on, in order.
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on any of the {@code BgeePreparedStatement}s 
     *                                  provided. 
     */
    public MySQLDAOResultSet(List<BgeePreparedStatement> statements) {
        this.statements = new ArrayList<BgeePreparedStatement>();
        this.addAllStatements(statements);
        this.executeNextStatementQuery();
    }

    @Override
    public boolean next() throws DAOException, QueryInterruptedException {
        log.entry();
        //If currentResultSet is null, it means that there are no more 
        //BgeePreparedStatement to obtain results from. False should have been 
        //already returned on the previous call, but maybe client is insisting...
        if (this.currentResultSet == null) {
            return log.exit(false);
        }
        this.checkCurrentStatementCanceled();
        
        try {
            //if we get at the end of the current ResultSet, try to execute the next 
            //BgeePreparedStatement in order
            if (!this.currentResultSet.next()) {
                this.executeNextStatementQuery();
                //no more BgeePreparedStatement to be executed
                if (this.currentResultSet == null) {
                    return log.exit(false);
                } 
                //otherwise, already position the cursor on the first result
                return log.exit(this.currentResultSet.next());
            }
            //otherwise, keep on iterating the current ResultSet
            return log.exit(true);
            
        } catch (SQLException e) {
            this.close();
            log.catching(e);
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public void close() throws DAOException {
        log.entry();
        this.closeCurrent();
        for (BgeePreparedStatement stmt: this.statements) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.catching(e);
                throw log.throwing(new DAOException(e));
            }
        }
        this.statements.clear();
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
            this.closeCurrent();
            this.currentStatement = this.statements.remove(0);
            this.checkCurrentStatementCanceled();
            this.currentResultSet = this.currentStatement.executeQuery();
        } catch (IndexOutOfBoundsException e) {
            //this simply means that we have no more BgeePreparedStatement to iterate.
            this.currentStatement = null;
            this.currentResultSet = null;
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
     * Closes {@link #currentResultSet} and {@link #currentStatement}, and sets 
     * these attributes to {@code null}.
     * @throws  DAOException if an error occurred while closing the {@code ResultSet} 
     *          or the {@code BgeePreparedStatement}.
     */
    private void closeCurrent() throws DAOException{
        log.entry();
        //closing the statement is supposed to close the ResultSet, but we are 
        //never too careful
        try {
            if (this.currentResultSet != null) {
                this.currentResultSet.close();
            }
            if (this.currentStatement != null) {
                this.currentStatement.close();
            }
        } catch (SQLException e) {
            //to avoid an infinite loop when calling close, which calls closeCurrent
            this.currentResultSet = null;
            this.currentStatement = null;
            //this is to close the remaining BgeePreparedStatements
            this.close();
            log.catching(e);
            throw log.throwing(new DAOException(e));
        }
        this.currentResultSet = null;
        this.currentStatement = null;
        log.exit();
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
    
    /**
     * Add {@code stmt} at the tail of the {@code List} of {@code BgeePreparedStatement} 
     * that should be executed by this {@code MysqlDAOResultSet}.
     * 
     * @param stmt  A {@code BgeePreparedStatement} to add to this {@code MysqlDAOResultSet}.
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on {@code stmt}. 
     */
    public void addStatement(BgeePreparedStatement stmt) throws IllegalArgumentException {
        this.addAllStatements(Arrays.asList(stmt));
    }
    
    /**
     * Add {@code statements} at the tail of the {@code List} of 
     * {@code BgeePreparedStatement} that should be executed by this 
     * {@code MysqlDAOResultSet}, in order.
     * 
     * @param stmt  A {@code List} of {@code BgeePreparedStatement}s to be added 
     *              to the tail of the {@code List} held by this {@code MysqlDAOResultSet}, 
     *              to be executed in order.
     * @throws IllegalArgumentException If {@code executeQuery} has been already called 
     *                                  on any of the {@code BgeePreparedStatement}s 
     *                                  provided. 
     */
    public void addAllStatements(List<BgeePreparedStatement> statements) 
            throws IllegalArgumentException {
        for (BgeePreparedStatement stmt: statements) {
            if (stmt.isExecuted()) {
                throw log.throwing(new IllegalArgumentException("A BgeePreparedStatement " +
                        "should not have been executed before being provided " +
                        "to the MySQLDAOResultSet"));
            }
        }
        this.statements.addAll(statements);
    }
    
    /**
     * @return  an {@code int} that is the number of {@code BgeePreparedStatement}s 
     *          currently held by this {@code MySQLDAOResultSet}.
     */
    int getStatementCount() {
        return this.statements.size();
    }
    
}
