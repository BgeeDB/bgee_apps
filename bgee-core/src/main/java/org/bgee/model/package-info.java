/**
 * Core layer (also known as "business layer") of the Bgee application. 
 * <h3>Starting up and releasing resources</h3>
 * Please note that: at the start of the Bgee application, 
 * {@link StartUpShutdown#startUpApplication()} should be called; at then end 
 * of the execution of a {@code Thread}, {@link StartUpShutdown#threadTerminated()} 
 * should be called, to release resources hold by it; at application complete shutdown, 
 * {@link StartUpShutdown#shutdownApplication()} should be called.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
package org.bgee.model;