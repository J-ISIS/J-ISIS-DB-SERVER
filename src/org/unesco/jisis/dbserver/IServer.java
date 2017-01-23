/*
 * IServer.java
 *
 * Created on 25 septembre 2007, 18:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.dbserver;

import org.unesco.jisis.corelib.server.ConfigProperties;



/**
 * The "public" interface for Servers; note that the Server instance
 * type will vary directly with the ServerManager used, in order to
 * best support the location transparency concept. IServer serves as
 * the Proxy to the Service instances loaded into the ServerManager;
 * any control of the Services <B>must</B> come through the Server,
 * since the client, if it tries to hold a Service instance within
 * its own JVM for "faster" access, may be holding a stale or
 * otherwise unstable reference.
 */
public interface IServer
    extends java.io.Serializable
{
    /**
     * Start the wrapped Service instance. Services have 15 seconds in which to
     * either initialize, or else start a thread to perform the necessary
     * initialization and return. If a Service fails to respond within 15
     * seconds of the start of its <code>start</code> call, the Server and/or
     * ServerManager are free to destroy it.
     */
    public boolean start();
    /**
     * Stop the wrapped Service instance; as with <code>start</code>, the Service
     * gets 15 seconds to stop itself before the ServerManager is free to take
     * more drastic steps.
     */
    public boolean stop();
    /**
     * Pauses the wrapped Service. The Service should respond within 15 seconds of
     * the start of this call; however, failure to do so is not sufficient grounds
     * for the ServerManager or Server to destroy it.
     */
    public boolean pause();
    /**
     * Resumes the wrapped Service. The Service should respond within 15 seconds of
     * the start of this call; however, failure to do so is not sufficient grounds
     * for the ServerManager or Server to destroy it.
     */
    public boolean resume();
    /**
     * Kills the wrapped Service.
     */
    public void kill();
        

    /**
     * Returns the state of the wrapped Service.
     */
    public String getState();
        

    /**
     * Returns the instance ID of the wrapped Service.
     */
    public String getInstanceID();
        
        
    /**
     * Returns the last Exception thrown, if any, by the wrapped Service.
     */
    public Exception getLastError();

    
    /**
     * Returns the Properties instance to use for configuration
     */
    public ConfigProperties getConfigInfo();
    /**
     * Set the Properties instance for this Service
     */
    public void setConfigInfo(ConfigProperties info);
}
