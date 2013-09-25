package org.bgee.model.dao.mysql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Abstraction layer to use a {@code java.sql.PreparedStatement}. 
 * <p>
 * This abstraction is needed to implement, for instance, 
 * methods defined by the JDBC 4.1 interfaces, but not yet implemented 
 * in the {@code Driver} used. 
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */
public class BgeePreparedStatement implements PreparedStatement, AutoCloseable
{
    /**
     * The {@code BgeeConnection} that was used 
     * to obtain this {@code BgeePreparedStatement}.
     * Used for notification purpose. 
     */
    private final BgeeConnection bgeeConnection;
    /**
     * The real {@code java.sql.PreparedStatement} that this class wraps.
     */
    private final PreparedStatement realPreparedStatement;
    /**
     * A {@code String} that represents the unique 
     * {@code BgeePreparedStatement} identification
     */
    private final String id;
    /**
     * Default constructor, should not be used. 
     * Constructor protected, so that only a {@link BgeeConnection} can provide 
     * a {@code BgeePreparedStatement}.
     * @throws SQLException 
     */
    protected BgeePreparedStatement() throws SQLException 
    {
        this(null, null,null);
    }
    /**
     * Constructor used to provide the real {@code java.sql.PreparedStatement} 
     * that this class wraps, and the {@code BgeeConnection} used to obtain 
     * this {@code BgeePreparedStatement}, for notification purpose.
     * <p>
     * Constructor protected, so that only a {@link BgeeConnection} can provide 
     * a {@code BgeePreparedStatement}.
     *  
     * @param id                        A {@code String} that represent the unique id
     *                                  of the {@code BgeePreparedStatement}. It has
     *                                  to be the hashed sql passed to the real
     *                                  {@code PreparedStatement}
     * 
     * @param connection				The {@code BgeeConnection} that was used 
     * 									to obtain this {@code BgeePreparedStatement}.
     *
     * @param realPreparedStatement     The {@code java.sql.PreparedStatement} 
     *                                  that this class wraps
     *                                                         
     */
    protected BgeePreparedStatement(String id,BgeeConnection connection,
            PreparedStatement realPreparedStatement)
    {
        this.bgeeConnection = connection;
        this.realPreparedStatement = realPreparedStatement;
        this.id = id;
    }    

    /**
     * @return the {@link #bgeeConnection}
     */
    private BgeeConnection getBgeeConnection() {
        return this.bgeeConnection;
    }
    /**
     * @return the {@link #realPreparedStatement}
     */
    private PreparedStatement getRealPreparedStatement() {
        return realPreparedStatement;
    }
    /**
     * @return A {@code String} that represents the unique 
     * {@code BgeePreparedStatement} identification
     */
    protected String getId(){
        return this.id;
    }
    /**
     * This method put back the {@code BgeePreparedStatement}
     * in the PreparedStatement Pool
     * instead of actually closing it and the underlying real {@code PreparedStatement}.
     * 
     * It clears the parameters of the statement before, 
     * 
     * @throws SQLException 
     */
    @Override
    public void close() throws SQLException {
        this.clearParameters();
        this.getBgeeConnection().addToPrepStatPool(this);
    }	


    //************************************************
    //  FORWARDING METHODS
    //************************************************
    /**
     * @param iface
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getRealPreparedStatement().unwrap(iface);
    }
    /**
     * @param sql
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeQuery(java.lang.String)
     */
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return getRealPreparedStatement().executeQuery(sql);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.PreparedStatement#executeQuery()
     */
    @Override
    public ResultSet executeQuery() throws SQLException {
        return getRealPreparedStatement().executeQuery();
    }
    /**
     * @param iface
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getRealPreparedStatement().isWrapperFor(iface);
    }
    /**
     * @param sql
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeUpdate(java.lang.String)
     */
    @Override
    public int executeUpdate(String sql) throws SQLException {
        return getRealPreparedStatement().executeUpdate(sql);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.PreparedStatement#executeUpdate()
     */
    @Override
    public int executeUpdate() throws SQLException {
        return getRealPreparedStatement().executeUpdate();
    }
    /**
     * @param parameterIndex
     * @param sqlType
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNull(int, int)
     */
    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getRealPreparedStatement().setNull(parameterIndex, sqlType);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getMaxFieldSize()
     */
    @Override
    public int getMaxFieldSize() throws SQLException {
        return getRealPreparedStatement().getMaxFieldSize();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBoolean(int, boolean)
     */
    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getRealPreparedStatement().setBoolean(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setByte(int, byte)
     */
    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        getRealPreparedStatement().setByte(parameterIndex, x);
    }
    /**
     * @param max
     * @throws SQLException
     * @see java.sql.Statement#setMaxFieldSize(int)
     */
    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        getRealPreparedStatement().setMaxFieldSize(max);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setShort(int, short)
     */
    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        getRealPreparedStatement().setShort(parameterIndex, x);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getMaxRows()
     */
    @Override
    public int getMaxRows() throws SQLException {
        return getRealPreparedStatement().getMaxRows();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setInt(int, int)
     */
    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        getRealPreparedStatement().setInt(parameterIndex, x);
    }
    /**
     * @param max
     * @throws SQLException
     * @see java.sql.Statement#setMaxRows(int)
     */
    @Override
    public void setMaxRows(int max) throws SQLException {
        getRealPreparedStatement().setMaxRows(max);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setLong(int, long)
     */
    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        getRealPreparedStatement().setLong(parameterIndex, x);
    }
    /**
     * @param enable
     * @throws SQLException
     * @see java.sql.Statement#setEscapeProcessing(boolean)
     */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        getRealPreparedStatement().setEscapeProcessing(enable);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setFloat(int, float)
     */
    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        getRealPreparedStatement().setFloat(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setDouble(int, double)
     */
    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        getRealPreparedStatement().setDouble(parameterIndex, x);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getQueryTimeout()
     */
    @Override
    public int getQueryTimeout() throws SQLException {
        return getRealPreparedStatement().getQueryTimeout();
    }
    /**
     * @param seconds
     * @throws SQLException
     * @see java.sql.Statement#setQueryTimeout(int)
     */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        getRealPreparedStatement().setQueryTimeout(seconds);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
     */
    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        getRealPreparedStatement().setBigDecimal(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setString(int, java.lang.String)
     */
    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        getRealPreparedStatement().setString(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBytes(int, byte[])
     */
    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getRealPreparedStatement().setBytes(parameterIndex, x);
    }
    /**
     * @throws SQLException
     * @see java.sql.Statement#cancel()
     */
    @Override
    public void cancel() throws SQLException {
        getRealPreparedStatement().cancel();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getWarnings()
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return getRealPreparedStatement().getWarnings();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
     */
    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        getRealPreparedStatement().setDate(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
     */
    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        getRealPreparedStatement().setTime(parameterIndex, x);
    }
    /**
     * @throws SQLException
     * @see java.sql.Statement#clearWarnings()
     */
    @Override
    public void clearWarnings() throws SQLException {
        getRealPreparedStatement().clearWarnings();
    }
    /**
     * @param name
     * @throws SQLException
     * @see java.sql.Statement#setCursorName(java.lang.String)
     */
    @Override
    public void setCursorName(String name) throws SQLException {
        getRealPreparedStatement().setCursorName(name);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        getRealPreparedStatement().setTimestamp(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
     */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        getRealPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }
    /**
     * @param sql
     * @return
     * @throws SQLException
     * @see java.sql.Statement#execute(java.lang.String)
     */
    @Override
    public boolean execute(String sql) throws SQLException {
        return getRealPreparedStatement().execute(sql);
    }
    /**
     * @param parameterIndex
     * @param x
     * @param length
     * @throws SQLException
     * @deprecated
     * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
     */
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        getRealPreparedStatement().setUnicodeStream(parameterIndex, x, length);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getResultSet()
     */
    @Override
    public ResultSet getResultSet() throws SQLException {
        return getRealPreparedStatement().getResultSet();
    }
    /**
     * @param parameterIndex
     * @param x
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
     */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        getRealPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getUpdateCount()
     */
    @Override
    public int getUpdateCount() throws SQLException {
        return getRealPreparedStatement().getUpdateCount();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getMoreResults()
     */
    @Override
    public boolean getMoreResults() throws SQLException {
        return getRealPreparedStatement().getMoreResults();
    }
    /**
     * @throws SQLException
     * @see java.sql.PreparedStatement#clearParameters()
     */
    @Override
    public void clearParameters() throws SQLException {
        getRealPreparedStatement().clearParameters();
    }
    /**
     * @param parameterIndex
     * @param x
     * @param targetSqlType
     * @throws SQLException
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        getRealPreparedStatement().setObject(parameterIndex, x, targetSqlType);
    }
    /**
     * @param direction
     * @throws SQLException
     * @see java.sql.Statement#setFetchDirection(int)
     */
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        getRealPreparedStatement().setFetchDirection(direction);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getFetchDirection()
     */
    @Override
    public int getFetchDirection() throws SQLException {
        return getRealPreparedStatement().getFetchDirection();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
     */
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        getRealPreparedStatement().setObject(parameterIndex, x);
    }
    /**
     * @param rows
     * @throws SQLException
     * @see java.sql.Statement#setFetchSize(int)
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {
        getRealPreparedStatement().setFetchSize(rows);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getFetchSize()
     */
    @Override
    public int getFetchSize() throws SQLException {
        return getRealPreparedStatement().getFetchSize();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getResultSetConcurrency()
     */
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return getRealPreparedStatement().getResultSetConcurrency();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.PreparedStatement#execute()
     */
    @Override
    public boolean execute() throws SQLException {
        return getRealPreparedStatement().execute();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getResultSetType()
     */
    @Override
    public int getResultSetType() throws SQLException {
        return getRealPreparedStatement().getResultSetType();
    }
    /**
     * @param sql
     * @throws SQLException
     * @see java.sql.Statement#addBatch(java.lang.String)
     */
    @Override
    public void addBatch(String sql) throws SQLException {
        getRealPreparedStatement().addBatch(sql);
    }
    /**
     * @throws SQLException
     * @see java.sql.Statement#clearBatch()
     */
    @Override
    public void clearBatch() throws SQLException {
        getRealPreparedStatement().clearBatch();
    }
    /**
     * @throws SQLException
     * @see java.sql.PreparedStatement#addBatch()
     */
    @Override
    public void addBatch() throws SQLException {
        getRealPreparedStatement().addBatch();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeBatch()
     */
    @Override
    public int[] executeBatch() throws SQLException {
        return getRealPreparedStatement().executeBatch();
    }
    /**
     * @param parameterIndex
     * @param reader
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
     */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        getRealPreparedStatement()
        .setCharacterStream(parameterIndex, reader, length);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
     */
    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        getRealPreparedStatement().setRef(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
     */
    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        getRealPreparedStatement().setBlob(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
     */
    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        getRealPreparedStatement().setClob(parameterIndex, x);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        return getRealPreparedStatement().getConnection();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
     */
    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        getRealPreparedStatement().setArray(parameterIndex, x);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.PreparedStatement#getMetaData()
     */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getRealPreparedStatement().getMetaData();
    }
    /**
     * @param current
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getMoreResults(int)
     */
    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return getRealPreparedStatement().getMoreResults(current);
    }
    /**
     * @param parameterIndex
     * @param x
     * @param cal
     * @throws SQLException
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
     */
    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        getRealPreparedStatement().setDate(parameterIndex, x, cal);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getGeneratedKeys()
     */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return getRealPreparedStatement().getGeneratedKeys();
    }
    /**
     * @param parameterIndex
     * @param x
     * @param cal
     * @throws SQLException
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
     */
    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        getRealPreparedStatement().setTime(parameterIndex, x, cal);
    }
    /**
     * @param sql
     * @param autoGeneratedKeys
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeUpdate(java.lang.String, int)
     */
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        return getRealPreparedStatement().executeUpdate(sql, autoGeneratedKeys);
    }
    /**
     * @param parameterIndex
     * @param x
     * @param cal
     * @throws SQLException
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
     */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        getRealPreparedStatement().setTimestamp(parameterIndex, x, cal);
    }
    /**
     * @param parameterIndex
     * @param sqlType
     * @param typeName
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
     */
    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName)
            throws SQLException {
        getRealPreparedStatement().setNull(parameterIndex, sqlType, typeName);
    }
    /**
     * @param sql
     * @param columnIndexes
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
     */
    @Override
    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException {
        return getRealPreparedStatement().executeUpdate(sql, columnIndexes);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
     */
    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        getRealPreparedStatement().setURL(parameterIndex, x);
    }
    /**
     * @param sql
     * @param columnNames
     * @return
     * @throws SQLException
     * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
     */
    @Override
    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException {
        return getRealPreparedStatement().executeUpdate(sql, columnNames);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.PreparedStatement#getParameterMetaData()
     */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return getRealPreparedStatement().getParameterMetaData();
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
     */
    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        getRealPreparedStatement().setRowId(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param value
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
     */
    @Override
    public void setNString(int parameterIndex, String value)
            throws SQLException {
        getRealPreparedStatement().setNString(parameterIndex, value);
    }
    /**
     * @param sql
     * @param autoGeneratedKeys
     * @return
     * @throws SQLException
     * @see java.sql.Statement#execute(java.lang.String, int)
     */
    @Override
    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
        return getRealPreparedStatement().execute(sql, autoGeneratedKeys);
    }
    /**
     * @param parameterIndex
     * @param value
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
     */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value,
            long length) throws SQLException {
        getRealPreparedStatement()
        .setNCharacterStream(parameterIndex, value, length);
    }
    /**
     * @param parameterIndex
     * @param value
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
     */
    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        getRealPreparedStatement().setNClob(parameterIndex, value);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
     */
    @Override
    public void setClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        getRealPreparedStatement().setClob(parameterIndex, reader, length);
    }
    /**
     * @param sql
     * @param columnIndexes
     * @return
     * @throws SQLException
     * @see java.sql.Statement#execute(java.lang.String, int[])
     */
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return getRealPreparedStatement().execute(sql, columnIndexes);
    }
    /**
     * @param parameterIndex
     * @param inputStream
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
     */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        getRealPreparedStatement().setBlob(parameterIndex, inputStream, length);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
     */
    @Override
    public void setNClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        getRealPreparedStatement().setNClob(parameterIndex, reader, length);
    }
    /**
     * @param sql
     * @param columnNames
     * @return
     * @throws SQLException
     * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean execute(String sql, String[] columnNames)
            throws SQLException {
        return getRealPreparedStatement().execute(sql, columnNames);
    }
    /**
     * @param parameterIndex
     * @param xmlObject
     * @throws SQLException
     * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
     */
    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
            throws SQLException {
        getRealPreparedStatement().setSQLXML(parameterIndex, xmlObject);
    }
    /**
     * @param parameterIndex
     * @param x
     * @param targetSqlType
     * @param scaleOrLength
     * @throws SQLException
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType,
            int scaleOrLength) throws SQLException {
        getRealPreparedStatement().setObject(parameterIndex, x, targetSqlType,
                scaleOrLength);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#getResultSetHoldability()
     */
    @Override
    public int getResultSetHoldability() throws SQLException {
        return getRealPreparedStatement().getResultSetHoldability();
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#isClosed()
     */
    @Override
    public boolean isClosed() throws SQLException {
        return getRealPreparedStatement().isClosed();
    }
    /**
     * @param poolable
     * @throws SQLException
     * @see java.sql.Statement#setPoolable(boolean)
     */
    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        getRealPreparedStatement().setPoolable(poolable);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#isPoolable()
     */
    @Override
    public boolean isPoolable() throws SQLException {
        return getRealPreparedStatement().isPoolable();
    }
    /**
     * @throws SQLException
     * @see java.sql.Statement#closeOnCompletion()
     */
    @Override
    public void closeOnCompletion() throws SQLException {
        getRealPreparedStatement().closeOnCompletion();
    }
    /**
     * @param parameterIndex
     * @param x
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
     */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        getRealPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }
    /**
     * @return
     * @throws SQLException
     * @see java.sql.Statement#isCloseOnCompletion()
     */
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return getRealPreparedStatement().isCloseOnCompletion();
    }
    /**
     * @param parameterIndex
     * @param x
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
     */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        getRealPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @param length
     * @throws SQLException
     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
     */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader,
            long length) throws SQLException {
        getRealPreparedStatement()
        .setCharacterStream(parameterIndex, reader, length);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
     */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x)
            throws SQLException {
        getRealPreparedStatement().setAsciiStream(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param x
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
     */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x)
            throws SQLException {
        getRealPreparedStatement().setBinaryStream(parameterIndex, x);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @throws SQLException
     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
     */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {
        getRealPreparedStatement().setCharacterStream(parameterIndex, reader);
    }
    /**
     * @param parameterIndex
     * @param value
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
     */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value)
            throws SQLException {
        getRealPreparedStatement().setNCharacterStream(parameterIndex, value);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @throws SQLException
     * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
     */
    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        getRealPreparedStatement().setClob(parameterIndex, reader);
    }
    /**
     * @param parameterIndex
     * @param inputStream
     * @throws SQLException
     * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
     */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream)
            throws SQLException {
        getRealPreparedStatement().setBlob(parameterIndex, inputStream);
    }
    /**
     * @param parameterIndex
     * @param reader
     * @throws SQLException
     * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
     */
    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        getRealPreparedStatement().setNClob(parameterIndex, reader);
    }

}
