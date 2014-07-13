package org.bgee.model.data.sql;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bgee.model.Parameters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class Database 
{
//	 The next serial number to be assigned
    protected static int nextSerialNum = 0;
    private static ThreadLocal<?> serialNum = new ThreadLocal<Object>() {
        @Override
		protected synchronized Object initialValue() {
        	return new Integer(nextSerialNum++);
        }
    };
    
    private static HashMap<Integer, HashMap<String, Database>> databasePool = 
    	new HashMap<Integer, HashMap<String, Database>>();
    
    
	/**
	 * status of the connection
	 * false : disconnected
	 * true : connected
	 */
	private boolean isConnected;
	/**
	 * Connection object for this login/pass/base/host
	 */
	private Connection connect;
	private boolean    				acceptQuery;
	private int         			runningQueryCount;
	private boolean 				reinitConnectionIfTooLong;
	
	private int     transactManager;
	private boolean rollback;
	
	private String login;
	private String password;
	private int    port;
	private String base;
	private String host;
	
	private Map<String, PreparedStatement> preparedStatements;
	private Calendar startCalendar;
	private Calendar stopCalendar;
	private long     startConnectionTime;
	private long     maxConnectionTime;
	    
    private Database()
    {
    	this.reinit();
    	this.reinitConnectionIfTooLong = false;
    	
    	this.startCalendar      = new GregorianCalendar();
    	this.stopCalendar       = new GregorianCalendar();
    	this.startConnectionTime = 0;
    	this.maxConnectionTime = 3600000;
    }
    
    private void reinit()
    {
    	this.transactManager     = 0;
    	this.rollback            = false;
    	
    	this.preparedStatements = new HashMap<String, PreparedStatement>();
    	this.setRunningQueryCount(0);
    	
    	this.setIsConnected(false);
    	this.setConnection(null);
    	this.setAcceptQuery(true);
    }
    
    private static synchronized int getIdentifier() {
        return ((Integer) (serialNum.get())).intValue();
    }
    
    public static synchronized Database getDatabase(String login, String password, int port, String base, 
    		String host, int debugLevel) 
    {
    	return Database.getDatabase(login, password, port, base, 
    			host, debugLevel, false);
    }
    
    public static synchronized Database getDatabase(String login, String password, int port, String base, 
    		String host, int debugLevel, boolean reconnectIfNecessary) 
    {
    	return Database.getDatabase(login, password, port, base, 
    			host, debugLevel, reconnectIfNecessary, 0);
    }
    
    /**
     * 
     * @param login
     * @param password
     * @param port
     * @param base
     * @param host
     * @param debugLevel
     * @param reconnectIfNecessary 	Note than when this argument is true, 
     * 								the connection will be reinitialize if opened for too long, 
     * 								and if there are no running queries. This means that after using 
     * 								<code>prepareStatement(String)</code>, the developer 
     * 								must absolutely use <code>endExecuteStatement(String)</code> 
     * 								(or <code>endExecuteStatement(String, Exception, boolean)</code> if the query failed)</code>
     * 								after having used <code>executeUpdate(PreparedStatement)</code>, 
     * 								or after finishing to use a <code>ResultSet</code> provided by 
     * 								<code>executeQuery(PreparedStatement)</code>. Otherwise, 
     * 								<code>endExecuteStatement(String)</code> will still see running queries.
     * 								After having used <code>endExecuteStatement(String)</code>, do not use 
     * 								any objects that you would previously get from <code>prepareStatement(String)</code>, 
     * 								as the connection might be closed. Instead, reuse once again 
     * 								<code>prepareStatement(String)</code>.
     * 								It's also mean that <code>runningQueryCount</code> should be always 
     * 								incremented before a call to <code>endExecuteStatement(String)</code> 
     * 								or <code>endExecuteStatement(String, Exception, boolean)</code>. 
     * 								This has to be done manually inside this class (see for instance 
     * 								<code>connection()</code> or <code>startTransaction(int)</code>)
     * @param maxConnectionTime 	Time in milliseconds. If <code>reconnectIfNecessary</code> is true, 
     * 								the connection will be closed after this time is elapsed, or 
     * 								after each query if this time is equal to 0.
     * 								
     * @return
     */
    public static synchronized Database getDatabase(String login, String password, int port, String base, 
    		String host, int debugLevel, boolean reconnectIfNecessary, long maxConnectionTime)
    {
    	int identifier = Database.getIdentifier();
    	
    	HashMap<String, Database> databaseCollection = 
    		Database.databasePool.get(new Integer(identifier));
    	
    	Database database = null;
    	String key = login + base + host;
    	
    	if (databaseCollection != null) {
    		database = databaseCollection.get(key);
    		if (database == null) {
        		database = new Database();
            	database.reinitConnectionIfTooLong = reconnectIfNecessary;
            	database.maxConnectionTime = maxConnectionTime;
        		databaseCollection.put(key, database);
        	}
    	} else {
    		databaseCollection = new HashMap<String, Database>();
    		database = new Database();
        	database.reinitConnectionIfTooLong = reconnectIfNecessary;
        	database.maxConnectionTime = maxConnectionTime;
    		databaseCollection.put(key, database);
    		Database.putIntoDatabasePool(databaseCollection);
    	}
    	
    	
    	if (!database.isConnected()) {
    		database.connection(login, password, port, base, host);
    	}
    	return database;
    }
    public static synchronized Database getDatabase()
    {
    	Parameters dbParam = Parameters.getParameters();
    	String login       = dbParam.getDBlogin();
    	String password    = dbParam.getDBpassword();
    	int    port        = dbParam.getDBport();
    	String base        = dbParam.getDBase();
    	String host        = dbParam.getDBhost();
    	int    debugLevel  = dbParam.getDebugLevel();
    	return Database.getDatabase(login, password, port, base, host, debugLevel);
    }
    
    private void connection(String localLogin, String localPassword, int localPort, 
    		String localBase, String localHost)
    {
    	this.setLogin(localLogin);
    	this.setPassword(localPassword);
    	this.setPort(localPort);
    	this.setBase(localBase);
    	this.setHost(localHost);
    	
    	this.connection();
    }
    
    private void connection()
    {
    	this.reinit();
    	
    	try{
    		//string modified for log4jdbc (see DriverSpy below)
    		String connectionString = "jdbc:log4jdbc:mysql://" + this.getHost() + ":" + 
		    this.getPort() + "/" + this.getBase() + "?user=" + 
		    this.getLogin() + "&password=" + this.getPassword();
    		
    		//DriverSpy allows to log all sql access
    	    Class.forName("net.sf.log4jdbc.DriverSpy").newInstance();
    	    this.setConnection(DriverManager.getConnection(connectionString));
    	    this.startConnectionTime = new GregorianCalendar().getTimeInMillis();
    	    this.setIsConnected(true);
    	    
    	} catch(Exception e){
    		this.setAcceptQuery(false);
    	} 
    }
    
    /**
     * Tells whether the connection should be reset
     * 
     * In Java, it is usual to have a "broken pipe" exception for connections 
     * holding for a very long time. This behavior is random, unpredictable, and happens 
     * even if a query was made seconds ago.
     * To avoid this, before performing a query to the database, we check whether 
     * this <code>connection</code> should be reconnect: check the connection time, 
     * whether there are no current transactions, ...
     * 
     * @return		true if the connection is opened for a long time and 
     * 				if there are no troubles to close it
     */
    private boolean shouldCloseConnection()
    {
    	//to avoid broken pipe, if the connection is opened for more than 1 hour
    	//but without breaking a transaction etc...
    	if (this.reinitConnectionIfTooLong && 
    			(this.maxConnectionTime == 0 || 
    					new GregorianCalendar().getTimeInMillis() - this.startConnectionTime > 
    	                this.maxConnectionTime) && 
    			this.isConnected() && 
    			!this.isCurrentTransaction() && 
    			this.getRunningQueryCount() == 0) {
    		return true;
    	}
    	return false;
    }
    
    private void reconnectIfNecessary()
    {
    	if (!this.isConnected()) {
    		this.connection();
    	}
    }
    
    private void deconnectIfNecessary()
    {
    	if (this.shouldCloseConnection()) {
    		this.close();
    	}
    }
    
    private void error()
    {
    	this.setAcceptQuery(false);
		if (this.isCurrentTransaction()) {
			this.rollback();
		}
    }
    
    public void close()
    {
    	if (this.isConnected()) {
    		try {
    			this.getConnection().close();
    	    	this.reinit();
    		} catch (SQLException e) {
    			this.setAcceptQuery(false);
    		}
    	}
    }
    
    public void startTransaction()
    {
    	this.startTransaction(4);
    }
    
    public void startTransaction(int level)
    {
        //if a second transaction is interleaved, 
        //the first one will not be stopped
    	if (this.transactManager == 0) {
    		
    		this.reconnectIfNecessary();
    		
    		int transactionLevel = 0;
    		switch (level) {
    		case 1 : 
    			transactionLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
    			break;
    		case 2 : 
    			transactionLevel = Connection.TRANSACTION_READ_COMMITTED;
    			break;
    		case 3 : 
    			transactionLevel = Connection.TRANSACTION_REPEATABLE_READ;
    			break;
    		case 4 : 
    			transactionLevel = Connection.TRANSACTION_SERIALIZABLE;
    			break;
    		}
    		try {
    		    this.getConnection().setAutoCommit(false);
    		    this.getConnection().setTransactionIsolation(transactionLevel);
    		} catch (SQLException e) {
    			this.setAcceptQuery(false);
    		}
    	}
    	this.transactManager++;
    }
    
    public void rollback()
    {
        //if try to rollback a transaction interleaved by error, 
        //wait for the end of the first one, and then rollback all of them.
    	if (this.transactManager > 1) {
    		this.rollback = true;
    	} else if (this.transactManager == 1) {
    		try {
    		    this.getConnection().rollback();
    		    this.getConnection().setAutoCommit(true);
    		    //will automatically decrements runningQueryCount
    		} catch (SQLException e) {
    			//don't call <code>error()</code> here, 
    			//(this method uses <code>rollback()</code>, 
    			//it would cause a infinite loop)
    			this.setAcceptQuery(false);
    		}
    		this.rollback = false;
    	}
    	if (this.transactManager > 0) {
    		this.transactManager--;
    	}
    	this.deconnectIfNecessary();
    }
    
    public void commit()
    {
        //commit is allowed only if no interleaved transaction was rollbacked
        if (this.transactManager == 1 && !this.rollback) {
        	try {
        	    this.getConnection().commit();
        	    this.getConnection().setAutoCommit(true);
        	} catch (SQLException e) {
        		this.error();
        	}
        } else if (this.transactManager == 1 && this.rollback) {
        	this.rollback();
        }
    	if (this.transactManager > 0) {
    		this.transactManager--;
    	}
    	this.deconnectIfNecessary();
    }
    
    public PreparedStatement prepareStatement(String sql)
    	    throws SQLException
    {
    	return this.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
    }
    
    /**
     * IMPORTANT: a call to this method should be ALWAYS followed at some point 
     * by a call to <code>endExecuteStatement(String)</code>, or 
     * <code>endExecuteStatement(String, Exception, boolean)</code> 
     * (after you got your resultSets, or after executing an update, etc). 
     * 
     * @param sql 				String representing the SQL query that will be used 
     * 							to create the preparedStatement. In the String, parameters are question marks
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys should be returned; 
     * 							one of <code>Statement.RETURN_GENERATED_KEYS</code> or 
     * 							<code>Statement.NO_GENERATED_KEYS</code> 
     * @return					a properly initiated <code>PreparedStatement</code>, 
     * 							containing the pre-compiled SQL statement, 
     * 							that will have the capability of returning auto-generated keys
     * @throws SQLException		if a database access error occurs, or the given parameter is not a Statement constant 
     * 							indicating whether auto-generated keys should be returned
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
    throws SQLException
    {
	    this.reconnectIfNecessary();
    	this.incrementsRunningQueryCount();
    	PreparedStatement preparedStatement = this.preparedStatements.get(sql);
    	//we initialize a new PreparedStatement if auto-generated keys are needed, 
    	//as we do not have the guarantee that the PreparedStatement stored has this capability. 
    	if (preparedStatement != null && autoGeneratedKeys != Statement.RETURN_GENERATED_KEYS) {
    		preparedStatement.clearParameters();
    	} else {
    	    preparedStatement = this.getConnection().prepareStatement(sql, autoGeneratedKeys);
    	    this.preparedStatements.put(sql, preparedStatement);
    	}
    	return preparedStatement;
    }

    public ResultSet executeQuery(PreparedStatement preparedStatement)
    throws SQLException
    {
    	this.startQuery();
    	ResultSet resultSet = preparedStatement.executeQuery();
    	this.endQuery();
    	return resultSet;
    }
    
    public int executeUpdate(PreparedStatement preparedStatement)
    throws SQLException
    {
    	this.startQuery();
    	int updateCount = preparedStatement.executeUpdate();
    	this.endQuery();
    	return updateCount;
    }
    
    /**
     * Don't forget that a call to <code>getGeneratedKeys()</code> 
     * must be done just after an insertion in the database, without any other command interleaved, 
     * and by providing the flag <code>Statement.RETURN_GENERATED_KEYS</code> 
     * to <code>Statement.executeUpdate()</code> or <code>Connection.prepareStatement()</code>.
     * 
     * @param preparedStatement
     * @return
     * @throws SQLException
     */
    public int getGeneratedKeys(PreparedStatement preparedStatement) 
    throws SQLException
    {
    	java.sql.ResultSet rs = preparedStatement.getGeneratedKeys();
        if (rs != null && rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    /**
     * to know if an error has occured
     * @return true if an error has occured
     */
    public boolean isError()
    {
    	if (this.getAcceptQuery()) {
    		return false;
    	}
		return true;
    }
    
    /**
     * A call to this method should be ALWAYS preceded by a call to 
     * <code>prepareStatement(String)</code>.
     * This method should never be used internally in the class <code>Database</code> 
     * (called only outside the class), to avoid generating infinite loop 
     * when catching errors. 
     * For a similar use inside this class, see <code>log(String)</code>.
     * 
     * @param query
     */
    public void endExecuteStatement(String query)
    {
    	this.decrementsRunningQueryCount();
    	this.deconnectIfNecessary();
    }
    
    /**
     * A call to this method should be ALWAYS preceded by a call to 
     * <code>prepareStatement(String)</code>.
     * This method should never be used internally in the class <code>Database</code> 
     * (called only outside the class), to avoid generating infinite loop
     * when catching errors. 
     * For a similar use inside this class, see <code>log(String, Exception, boolean)</code>.
     * 
     * @param query
     * @param exception
     * @param forceQuery
     */
    public void endExecuteStatement(String query, Exception exception, boolean forceQuery)
    {
    	if (!forceQuery) {
			this.error();
		}
    	this.decrementsRunningQueryCount();
    	this.deconnectIfNecessary();
    }
    
    public static synchronized void destructAll()
    {
    	HashMap<String, Database> databaseCollection = 
    		Database.removeFromDatabasePool();
    	
    	if (databaseCollection != null) {
    		Iterator<Database> iterateDatabase = databaseCollection.values().iterator();
    		
    		while (iterateDatabase.hasNext()) {
    			Database database = iterateDatabase.next();
    			database.destruct();
    		}
    	}
    }
    
    private void destruct()
    {
    	if (this.transactManager> 0) {
    		this.rollback();
    	}
    	this.close();
    }
    
    private static HashMap<String, Database> removeFromDatabasePool()
    {
    	return Database.databasePool.remove(new Integer(Database.getIdentifier()));
    }
    
    private static void 
    putIntoDatabasePool(HashMap<String, Database> databaseCollection)
    {
    	Database.databasePool.put(new Integer(Database.getIdentifier()), databaseCollection);
    }
    
    public String getLogin()
    {
    	return this.login;
    }
    private void setLogin(String login)
    {
    	this.login = login;
    }
    private String getPassword()
    {
    	return this.password;
    }
    private void setPassword(String password)
    {
    	this.password = password;
    }
    public int getPort()
    {
    	return this.port;
    }
    private void setPort(int port)
    {
    	this.port = port;
    }
    public String getBase()
    {
    	return this.base;
    }
    private void setBase(String base)
    {
    	this.base = base;
    }
    public String getHost()
    {
    	return this.host;
    }
    private void setHost(String host)
    {
    	this.host = host;
    }
    
    public boolean isConnected()
    {
    	return this.isConnected;
    }
    private void setIsConnected(boolean isConnected)
    {
    	this.isConnected = isConnected;
    }
    
    private void setConnection (Connection connection)
    {
    	this.connect = connection;
    }
    public Connection getConnection()
    {
    	return this.connect;
    }
    
    public boolean getAcceptQuery()
    {
    	return this.acceptQuery;
    }
    private void setAcceptQuery(boolean acceptQuery)
    {
    	this.acceptQuery = acceptQuery;
    }
    
    private boolean isCurrentTransaction()
    {
    	if (this.transactManager > 0) {
    		return true;
    	}
		return false;
    }
    
    private void startQuery()
    {
    	this.startCalendar.setTime(new Date());
    }
    private void endQuery()
    {
    	this.stopCalendar.setTime(new Date());
    }

	private int getRunningQueryCount() 
	{
		return this.runningQueryCount;
	}
	private void setRunningQueryCount(int runningQueryCount) 
	{
		this.runningQueryCount = runningQueryCount;
	}
	private void incrementsRunningQueryCount() 
	{
		this.runningQueryCount++;
	}
	private void decrementsRunningQueryCount() 
	{
		this.runningQueryCount--;
	}
}
