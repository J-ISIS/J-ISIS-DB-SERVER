/*
 * IServerManager.java
 *
 * Created on 25 septembre 2007, 18:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.dbserver;


import java.util.Properties;
import org.unesco.jisis.corelib.server.ConfigProperties;
import org.unesco.jisis.corelib.server.IService;
import org.unesco.jisis.dbserver.classloader.IClassLoaderStrategy;



/**
 * This is the ServerManager interface. Any and all "public"
 * ServerManager API calls must be exposed through here.
 */
public interface IServerManager
{
    /**
     *
     */
    public void shutdown();
    

    /**
     *
     */
    public void deployService(String serviceName, 
                              IClassLoaderStrategy strategy);
    
    
    /**
     *
     */
    public IServer loadService(IService svc);
    /**
     *
     */
    public IServer loadService(String svcName);
    /**
     *
     */
    public IServer addService(IService svc, ConfigProperties args);
    /**
     *
     */
    public IServer addService(String svcName, 
                              ConfigProperties args);
    /**
     *
     */
    public void removeService(String instanceID);
    /**
     *
     */
    public void killService(String instanceID);
    /**
     *
     */
    public String[] getServices();
    /**
     *
     */
    public IServer getService(String instanceID);


    /**
     *
     */
    public void log(String msg);
    /**
     *
     */
    public void log(Exception ex);
    /**
     *
     */
    public void error(String msg);
    /**
     *
     */
    public void error(Exception ex);
}
