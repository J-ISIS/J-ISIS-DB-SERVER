package org.unesco.jisis.dbserver;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.unesco.jisis.corelib.server.ConfigProperties;
import org.unesco.jisis.corelib.server.IService;


/**
 * Server wraps the Service instance, using Future calls to help preserve
 * the responsiveness and robustness of the ServerManager.
 */
public class LocalServer
    implements IServer
{
    // Internal data
    //
    private IService m_service = null;
    private Exception m_exception = null;
    
    
    // Prevent no-arg object instantiation
    //
    private LocalServer()
    {}

    /**
     * Construct a Server around a Service instance.
     */
    public LocalServer(IService svc)
    {
        m_service = svc;
    }


    /**
     * Start the wrapped Service instance. Services have 15 seconds in which to
     * either initialize, or else start a thread to perform the necessary
     * initialization and return. If a Service fails to respond within 15
     * seconds of the start of its <code>start</code> call, the Server and/or
     * ServerManager are free to destroy it.
     */
    public boolean start()
    {
        ServerManager.log("LocalServer.start()");

        if (m_service == null)
        {
            ServerManager.log("m_service == null?!?");
        }

        // We want to fire off a Thread to make the start() call, and wait
        // up to 15 seconds to see if we return. If we don't by the time the
        // 15 seconds are up, we assume the Service has run off into Limbo
        // and needs to be killed. (Most Services of any complexity will need
        // to fire off their own Thread to do their work, so their start()
        // methods should come back pretty quickly.)
        //

        try
        {
           ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {

                public Object call()
                {
                    try
                    {
                        m_service.start();
                        ServerManager.log(
                            m_service.getClass().getName() + ": started");
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.error(ex);
                    }
                    return null;
                }
            });
            executor.execute(futureTask);

            
            futureTask.get(15*1000,TimeUnit.MILLISECONDS);
                // we want to wait 15 seconds, no more.

            return true;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.log(iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.error(ex);
        }
        return false;
    }
    /**
     * Stop the wrapped Service instance; as with <code>start</code>, the Service
     * gets 15 seconds to stop itself before the ServerManager is free to take
     * more drastic steps.
     */
    public boolean stop()
    {
        // We use the method described in start() to make sure calls to
        // stop() don't wander off into Limbo.
        //
        try
        {
           ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {


                public Object call()
                {
                    try
                    {
                        m_service.stop();
                        ServerManager.instance().log(
                            m_service.getClass().getName() + ": stopped");
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.instance().log(ex);
                    }
                    return null;
                }
            });
            executor.execute(futureTask);


            futureTask.get(15*1000,TimeUnit.MILLISECONDS);
            // we want to wait 15 seconds, no more.

            return true;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.instance().log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.instance().log(Thread.currentThread().toString() + " " + iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.instance().log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.instance().log(ex);
        }
        return false;
    }
    /**
     * Pauses the wrapped Service. The Service should respond within 15 seconds of
     * the start of this call; however, failure to do so is not sufficient grounds
     * for the ServerManager or Server to destroy it.
     */
    public boolean pause()
    {
        // We use the method described in start() to make sure calls to
        // pause() don't wander off into Limbo.
        //
        try
        {
           ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {



                public Object call()
                {
                    try
                    {
                        m_service.pause();
                        ServerManager.instance().log(
                            m_service.getClass().getName() + ": paused");
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.instance().log(ex);
                    }
                    return null;
                }
            });
            executor.execute(futureTask);

            futureTask.get(15*1000,TimeUnit.MILLISECONDS);
            // we want to wait 15 seconds, no more.

            return true;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.instance().log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.instance().log(iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.instance().log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.instance().log(ex);
        }
        return false;
    }
    /**
     * Resumes the wrapped Service. The Service should respond within 15 seconds of
     * the start of this call; however, failure to do so is not sufficient grounds
     * for the ServerManager or Server to destroy it.
     */
    public boolean resume()
    {
        // We use the method described in start() to make sure calls to
        // resume() don't wander off into Limbo.
        //
        try
        {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {
            
                public Object call()
                {
                    try
                    {
                        m_service.resume();
                        ServerManager.instance().log(
                            m_service.getClass().getName() + ": resumed");
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.instance().log(ex);
                    }
                    return null;
                }
            });
             executor.execute(futureTask);

            futureTask.get(15*1000,TimeUnit.MILLISECONDS);
            // we want to wait 15 seconds, no more.

            return true;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.instance().log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.instance().log(iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.instance().log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.instance().log(ex);
        }
        return false;
    }
    /**
     *
     */
    public void kill()
    {
        m_service = null;
        System.gc();
    }


    /**
     * 
     */
    public String getState()
    {
        // We use the method described in start() to make sure calls to
        // getState() don't wander off into Limbo.
        //
        try
        {
           ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {
           
                public Object call()
                {
                    try
                    {
                        return m_service.getState();
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.instance().log(ex);
                    }
                    return null;
                }
            });
             executor.execute(futureTask);

             String result = (String)futureTask.get(15*1000,TimeUnit.MILLISECONDS);
             // we want to wait 15 seconds, no more.

            return result;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.instance().log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.instance().log(iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.instance().log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.instance().log(ex);
        }
        return null;
    }


    /**
     *
     */
    public String getInstanceID()
    {
        // We use the method described in start() to make sure calls to
        // getInstanceID() don't wander off into Limbo.
        //
        try
        {
           ExecutorService executor = Executors.newFixedThreadPool(1);

            FutureTask<Object> futureTask = new FutureTask<Object>(
                    new Callable<Object>()
                {
           
                public Object call()
                {
                    try
                    {
                        return m_service.getInstanceID() + "/" +
                            m_service.getClass().getClassLoader().toString();
                    }
                    catch (Exception ex)
                    {
                        m_exception = ex;
                        ServerManager.instance().log(ex);
                    }
                    return null;
                }
            });
            executor.execute(futureTask);

             String result = (String)futureTask.get(15*1000,TimeUnit.MILLISECONDS);
            // we want to wait 15 seconds, no more.

            return result;
        }
        catch (TimeoutException tEx)
        {
            m_exception = tEx;

            // The Service ran out of time starting up; kill it, note the
            // failure to start, and return
            //
            ServerManager.instance().log(tEx);
        }
        catch (InterruptedException iEx)
        {
            m_exception = iEx;

            // For some reason, the thread doing the call failed; note the
            // failure to start, and return
            //
            ServerManager.instance().log(iEx);
        }
        catch (ExecutionException itEx)
        {
            m_exception = itEx;

            // Java Reflection failed; note the failure, and return
            //
            ServerManager.instance().log(itEx);
        }
        catch (Exception ex)
        {
            m_exception = ex;
            ServerManager.instance().log(ex);
        }
        return null;
    }


    /**
     *
     */
    public Exception getLastError()
    {
        return m_exception;
    }


    /**
     * Returns the Properties instance to use for configuration
     */
    public ConfigProperties getConfigInfo()
    {
        return m_service.getConfigInfo();
    }
    /**
     * Set the Properties instance for this Service
     */
    public void setConfigInfo(ConfigProperties info)
    {
        m_service.setConfigInfo(info);
    }
}       