/**
 * The {@code bgee-pipeline} module is used to generate and insert data into 
 * the Bgee database. Most classes of this module are used "stand-alone", they provide 
 * a {@code main} method to be launched. These classes use the {@code bgee-dao-sql} module 
 * directly, they do not use it as a {@code service provider} of the {@code bgee-dao-api} 
 * module (as the {@code bgee-core} module does). This is because the insertion/update 
 * methods are not part of the API, but it is still useful to implement them 
 * in the {@code bgee-dao-sql} module, to benefit from its capacity to connect to database, 
 * etc. The {@code bgee-dao-api} module is only used for its capacity to properly load 
 * a {@code DAOManager} and to provide parameters to it.
 * <p>
 * The principle to provide arguments and parameters to the classes of this module is always 
 * the same: parameters used for other modules are provided in the same way than in 
 * these modules, and parameters specific to the classes used here are provided to 
 * the {@code main} method.
 * <p>
 * For instance, the parameters to connect to the database should be provided as specified 
 * by the {@code bgee-dao-api} and {@code bgee-dao-sql} modules: either as System 
 * properties, are via a property file. Parameters specific to the classes launched 
 * are provided to the {@code main} method. Example:
 * {@code java -Dbgee.dao.jdbc.driver.names=my.jdbc.driver -Dbgee.dao.jdbc.url=myConnectionUrl MyPipelineClass myPipelineArgument1 myPipelineArgument2 ...}
 * <p>
 * Most likely, you will want to make the {@code bgee-dao-sql} module to use a JDBC 
 * {@code Driver}, and not a {@code DataSource}, as you will most likely run one class at a time, 
 * and those classes use one connection at a time. It is recommended to use the logging 
 * library {@code log4jdbc-log4j2}, and the {@code PreparedStatement} pooling library 
 * {@code psp4jdbc}. It is maybe not needed to use the query cache {@code easycache4jdbc}. 
 * So the properties to provide should be something like: 
 * {@code java -Dbgee.dao.jdbc.driver.names=com.mysql.jdbc.Driver,net.sf.log4jdbc.sql.jdbcapi.DriverSpy,org.bgee.psp4jdbc.jdbcapi.Driver -Dbgee.dao.jdbc.url=jdbc:psp4jdbc:log4jdbc:mysql://127.0.0.1:3306/bgee_vXX?user=xxx&password=xxx}
 * (note the "jdbc:psp4jdbc:log4jdbc:" fragment at the beginning of the connection URL, 
 * to properly load {@code psp4jdbc} and {@code log4jdbc-log4j2}).
 * You can also provide them in a property file put in the classpath, named 
 * {@code bgee.dao.properties}, so that you don't need to provide them in the command line.
 * 
 * @author Frederic Bastian
 */
package org.bgee.pipeline;