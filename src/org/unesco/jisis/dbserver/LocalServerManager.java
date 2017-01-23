package org.unesco.jisis.dbserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import org.unesco.jisis.corelib.server.ConfigProperties;
import org.unesco.jisis.corelib.server.IService;
import org.unesco.jisis.dbserver.classloader.HashtableClassLoader;
import org.unesco.jisis.dbserver.classloader.IClassLoaderStrategy;
import org.unesco.jisis.dbserver.classloader.StrategyClassLoader;



/**
 * This class presents a local-to-this-JVM-only ServerManager.
 * It is useful for localized testing, and for loading/running
 * Services within their own JVM. Note that use of this
 * ServerManager does not inherently prevent object-sharing or
 * prevent inter-JVM communication of Services, since it does
 * nothing to block sockets or any other IPC communication. For
 * example, nothing prevents us from running a LocalServerManager
 * with a SocketControlService that allows us to remotely (through
 * the SocketControlService) start, stop and otherwise control
 * the Services listed within this JVM.
 *
 * <P>Note that LocalServerManager, by default, uses the local
 * (default) ClassLoader scheme to load and find its classes,
 * so any classes loaded will need to be found on the CLASSPATH
 * and/or as an extension.
 */
public class LocalServerManager implements IServerManager {

    // Internal data
    //
    private Dictionary m_servers = new Hashtable();
    private HashMap m_serviceLoaders = new HashMap();

    private OutputStream m_logStream = null;
    private OutputStream m_errStream = System.err;
    private PrintWriter m_log = null;
    private PrintWriter m_err = new PrintWriter(m_errStream);

    /**
     *
     */
    public LocalServerManager() {
        ServerManager.instance(this);

        // Set log & error streams
        try {
            m_logStream = new FileOutputStream("ServerManager.log");
            m_errStream = System.out;
            m_log = new PrintWriter(m_logStream);
            m_err = new PrintWriter(m_errStream);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    //===========================================
    // IServerManager-inherited methods (implementations)
    //
    /**
     * Shut the entire system down, usually in preparation for terminating this VM (or perhaps for doing a
     * complete shutdown/restart cycling). Effectively, this is the same as calling <code>getServices</code>
     * to get all Servers' instanceIDs, then calling <code>removeService</code> on each one.
     */
    public void shutdown() {
        log("Entering ServerManager.shutdown()");

        // Get a list of all the running instances, and try to removeService on
        // each one.
        //
        String[] svcs = getServices();
        for (int i = 0; i < svcs.length; i++) {
            log("Shutting down " + svcs[i]);
            removeService(svcs[i]);
        }

        log("Exiting ServerManager.shutdown()");
    }


    /**
     * Place a ClassLoaderStrategy into the service-loaders map,
     * so subsequent addService() calls can use the loader to
     * retrieve the necessary code.
     * @param serviceName
     * @param strategy
     */
    @Override
    public void deployService(String serviceName,
        IClassLoaderStrategy strategy) {
        log("Entering ServerManager.deployService");

        m_serviceLoaders.put(serviceName, strategy);

        log("Exiting ServerManager.deployService");
    }
    
    
    /**
     *
     * @param svc
     * @return 
     */
    @Override
    public IServer loadService(IService svc) {
        log("Service " + svc.toString() + "("
            + svc.getClass().getName() + " "
            + svc.getClass().getClassLoader().toString()
            + ") created");

        // Wrap our Service up in a LocalServer wrapper object
        IServer svr = new LocalServer(svc);
        log("IServer created");

        // Drop it in our Dictionary of Servers....
        m_servers.put(svr.getInstanceID(), svr);

        return svr;
    }
    /**
     *
     */
    public IServer loadService(String svcName) {
        try {
            log("Entering ServerManager.loadService(String)");

            // Get the ClassLoaderStrategy corresponding to the
            // service name
            IClassLoaderStrategy strat
                = (IClassLoaderStrategy) m_serviceLoaders.get(svcName);

            StrategyClassLoader scl
                = new StrategyClassLoader(strat);
            IService svc
                = (IService) scl.loadClass(svcName).newInstance();

            log("ServerManager.loadService(String) successful");
            return loadService(svc);
        } catch (Exception ex) {
            error(ex);
            return null;
        } finally {
            log("Exiting ServerManager.loadService(String)");
        }
    }
    /**
     * Add the loaded Service to the list of Servers and start it
     */
    public IServer addService(IService svc, ConfigProperties args) {
        log("Entering ServerManager.addService()");

        try {
            log("Service " + svc.toString() + "("
                + svc.getClass().getName() + " "
                + svc.getClass().getClassLoader().toString()
                + ") created");

            // Wrap our Service up in a LocalServer wrapper object
            IServer svr = new LocalServer(svc);

            // Drop it in our Dictionary of Servers....
            m_servers.put(svr.getInstanceID(), svr);

            // Configure it
            svr.setConfigInfo(args);

            // Start it; if the start fails, remove it
            if (svr.start()) {
                log("Service started");
                return svr;
            } else {
                // Log the exception (if any) that caused the Service to fail
                PrintWriter pw = new PrintWriter(getLogStream());
                svr.getLastError().printStackTrace(pw);
                pw.flush();

                removeService(svr.getInstanceID());
                return null;
            }
        } catch (Throwable ex) {
            // Something "wrong" happened; in a production system, you probably
            // want to do something a bit more proactive here.
            PrintWriter pw = new PrintWriter(getLogStream());
            ex.printStackTrace(pw);
            pw.flush();
            return null;
        } finally {
            log("Exiting ServerManager.addService()");
        }
    }

    /**
     * Add a Service by name; this presumes that the Service has already been deployed to this ServerManager
     * via the deployService method.
     */
    public IServer addService(String svcName, ConfigProperties args) {
        try {
            log("Entering ServerManager.loadService(String)");

            // Get the ClassLoaderStrategy corresponding to the
            // service name
            IClassLoaderStrategy strat
                = (IClassLoaderStrategy) m_serviceLoaders.get(svcName);
            //if (strat == null)
            //{
            //    return null;
            //}

            StrategyClassLoader scl
                = new StrategyClassLoader(strat);
            IService svc
                = (IService) scl.loadClass(svcName).newInstance();

            return addService(svc, args);
        } catch (Exception ex) {
            error(ex);
            return null;
        } finally {
            log("Exiting ServerManager.addService(String, String[])");
        }
    }

    /**
     * Attempt to stop (if necessary) and remove an instance of a Server. Because it's possible that multiple
     * Servers of a given type can be running simultaneously (for example, sockets-based Services listening on
     * multiple ports), we need to have the user identify which Server they wish shut down by using the Server
     * instance's instanceID.
     */
    public void removeService(String instanceID) {
        try {
            log("Entering ServerManager.removeService()");

            // Find the service given by 'instanceID'
            //
            IServer svr = getService(instanceID);
            if (svr != null) {
                // If it's still running, order it to stop
                //
                String svrState = svr.getState();
                if (!svrState.equals(IService.STOPPED) && !svrState.equals(IService.PAUSED)) {
                    svr.stop();
                }

                // Remove it from the Dictionary
                //
                log("Removing " + instanceID + " from system.");
                m_servers.remove(instanceID);
            }
        } finally {
            log("Exiting ServerManager.removeService()");
        }
    }

    /**
     * Try to kill the Service--don't try to stop() it
     */
    public void killService(String instanceID) {
        m_servers.remove(instanceID);
        System.gc();
    }

    /**
     * Obtain a list of every Server instance running in the system.
     */
    public String[] getServices() {
        log("Entering ServerManager.getServices()");

        String[] svrArray = new String[m_servers.size()];

        int ctr = 0;
        String list = new String("{\n");
        for (java.util.Enumeration e = m_servers.keys(); e.hasMoreElements();) {
            svrArray[ctr] = (String) e.nextElement();
            list += "   " + svrArray[ctr++] + "\n";
        }
        list += "}";

        log("Exiting ServerManager.getServices(); list = " + list);
        return svrArray;
    }

    /**
     * Obtain a reference to a Server instance by ID. If it can't be found (perhaps it's shut down since the
     * user obtained the ID?), then return a null instance.
     */
    public IServer getService(String instanceID) {
        return (IServer) m_servers.get(instanceID);
    }

    /**
     *
     */
    public void log(String msg) {
        if (m_log != null) {
            StringBuffer m = new StringBuffer();
            m.append(new Date());
            m.append(" [");
            m.append(Thread.currentThread().toString());
            m.append("]: ");
            m.append(msg);

            m_log.println(m);
            System.out.println(m);
            m_log.flush();
        }
    }

    /**
     *
     */
    public void log(Exception ex) {
        if (m_log != null) {
            log("Exception raised: " + ex.toString());
            PrintWriter pw = new PrintWriter(getLogStream());
            pw.println(new Date() + " Exception raised: " + ex.toString());
            ex.printStackTrace(pw);
            pw.flush();
        }
    }

    /**
     *
     */
    public void error(String msg) {
        if (m_err != null) {
            StringBuffer m = new StringBuffer();
            m.append(new Date());
            m.append(" [");
            m.append(Thread.currentThread().toString());
            m.append("]: *** ERROR *** ");
            m.append(msg);

            m_err.println(m);
            m_err.flush();
        }
    }

    /**
     *
     */
    public void error(Exception ex) {
        if (m_err != null) {
            error(": Exception raised: " + ex.toString());
            PrintWriter pw = new PrintWriter(getErrStream());
            pw.println(new Date() + " Exception raised: " + ex.toString());
            ex.printStackTrace(pw);
            pw.flush();
        }
    }


    //===========================================
    // LocalServerManager-specific methods
    //

    /**
     * Return the OutputStream used for writing to the log.
     */
    public OutputStream getLogStream()
    {
        return m_logStream;
    }
    /**
     * Set the OutputStream used for writing to the log.
     */
    public void setLogStream(OutputStream os)
    {
        m_logStream = os;
        if (m_logStream != null)
            m_log = new PrintWriter(m_logStream);
        else
            m_log = null;
    }
    /**
     * Return the OutputStream used for writing errors.
     */
    public OutputStream getErrStream()
    {
        return m_errStream;
    }
    /**
     * Set the OutputStream used for writing errors. On your head
     * be the consequences if you set this to null!
     */
    public void setErrStream(OutputStream os)
    {
        m_errStream = os;
        if (m_errStream != null)
            m_err = new PrintWriter(m_errStream);
        else
            m_err = null;
    }       


    /**
     * This is the entry point of the LocalServerManager system; it
     * creates an instance of LocalServerManager (which in turn
     * registers itself as the one-and-only ServerManager instance),
     * then parses the command line for arguments indicating which
     * Services to load and start.
     */
    public static void main(String[] args)
    {
        // Create the IServerManager instance; registers itself with
        // the static ServerManager class
        //
        new LocalServerManager();

        ServerManager.log("Entering LocalServerManager.main()");

        // Parse command-line arguments, if any
        //
        if (args == null || args.length == 0)
        {
            // Print LocalServerManager usage
            //
            System.out.println("LocalServerManager usage: ");
            System.out.println("");
            System.out.println("\tjava LocalServerManager " +
                               "[options] \"<class-to-load> " +
                               "<arg0> <arg1> ... <argn>\"");
            System.out.println("");
            System.out.println("where ");
            System.out.println("");
            System.out.println("options:");
            System.out.println("\t@<filename>: filename to use as" +
                               " list of Services to load");
            System.out.println("");
            System.out.println("\tNote: if the arguments must be " +
                               "in a quoted string, use the '@' " + 
                               "form; the command-line version " +
                               "doesn't deal well with quoted args.");
        }

        for (int argc=0; argc < args.length; argc++)
        {
            if (args[argc].startsWith("@"))
            {
                // The "@" argument indicates the file we should
                // parse for services to execute
                try
                {
                    String arg = args[argc];
                    String filename = 
                        arg.substring(arg.indexOf("@")+1, arg.length());
                    FileInputStream fis = 
                        new FileInputStream(filename);
                    ServerManager.parseInputStream(fis);
                }
                catch (Exception ex)
                {
                    // Ignore it and move on

                    ex.printStackTrace();
                }
            }
            else if ("TEST".equals(args[argc]))
            {
                // Deploy a Service, then try to add it.
                try
                {
                    // Look for "TestService.class" in the current
                    // directory
                    String filename = "TestService.class";
                    java.io.FileInputStream fis =
                        new java.io.FileInputStream(filename);

                    byte[] bytes = new byte[fis.available()];
                    fis.read(bytes);

                    // Create a HashtableClassLoader
                    HashtableClassLoader
                        hcl = new HashtableClassLoader();
                    hcl.putClass("TestService", bytes);

                    // Deploy it
                    ServerManager.deployService("TestService", hcl);

                    // Now add the Service
                    IServer svr = 
                        ServerManager.addService("TestService", 
                                                 new ConfigProperties());
                    if (svr == null)
                    {
                        System.out.println("Test failed!");
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                ServerManager.parseArg(args[argc]);
            }
        }
        
        ServerManager.log("Exiting LocalServerManager.main()");
    }

    
  
}