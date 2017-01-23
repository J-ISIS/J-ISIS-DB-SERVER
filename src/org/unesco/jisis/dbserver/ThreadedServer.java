package org.unesco.jisis.dbserver;

import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.server.IService;

/**
 * ThreadedServer
 */
public abstract class ThreadedServer implements IService {

    // Internal data
    //
    private Thread m_thread = null;
    private Runnable m_runnable = null;
    private String m_state = STOPPED;
    protected boolean m_paused = false;
    protected boolean m_shouldStop = false;
    
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConsoleThread.class);

    /**
     *
     * @throws java.lang.Exception
     */
    @Override
    public void start()
        throws Exception {
        // We're starting
        //
        if (!getState().equals(STARTING)) {
            setState(STARTING);
        }

        // Start our thread
        //
        if (m_thread == null) {
            m_thread = new Thread(new ThreadGroup(this.toString()),
                m_runnable, getClass().getName());
        }
        m_thread.start();

        // We're running
        //
        setState(RUNNING);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Override
    public void stop()
        throws Exception {
        // We're stopping
        //
        if (!getState().equals(STOPPING)) {
            setState(STOPPING);
        }

        // Sanity-check--did the Thread fail to initialize?
        //
        if (m_thread == null) {
            return;
        }

        // First we'll try the easy way
        //            
        m_shouldStop = true;

        // Stop our thread; this assumes that the thread is written to be
        // sensitive to interrupts (that is, it checks isInterrupted() in
        // a timely fashion). If it doesn't respond within 10 seconds,
        // notify the system so a user can perhaps kill() it.
        //
        ServerManager.log(
            "Asking thread " + m_thread + " to stop.");
        m_thread.interrupt();
        m_thread.join(10 * 1000);
            // Wait for thread to finish for 10 seconds; if we're not back
        // by then, we'll move on

        if (m_thread.isAlive()) {
            LOGGER.info(
                "ThreadedServer for " + getClass().getName() + ": "
                + "Thread refuses to stop within 10 seconds.");
            return;
        }

        // We've stopped
        //
        setState(STOPPED);
    }

    /**
     *
     */
    public void kill() {
        // Sanity-check--did the Thread fail to initialize?
        //
        if (m_thread == null) {
            return;
        }

        // If we tried to stop, or thought we stopped, and the thread
        // is still alive, kill it. Note that this implementation WILL
        // generate deprecation warnings due to the call to stop(); if
        // this bothers you, comment this entire method out.
        if ((getState().equals(STOPPED) && m_thread.isAlive())
            || (getState().equals(STOPPING) && m_thread.isAlive())) {
            LOGGER.info(
                "ThreadedServer for " + getClass().getName() + ":"
                + "Calling stop() on Thread.");
            m_thread.interrupt();

            setState(STOPPED);
        }
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Override
    public void pause()
        throws Exception {
        // We're pausing
        //
        if (!getState().equals(PAUSING)) {
            setState(PAUSING);
        }

        // Sanity-check--did the Thread fail to initialize?
        //
        if (m_thread == null) {
            return;
        }

        // Set the 'paused' member to true
        //
        m_paused = true;

        // If you prefer a more decisive approach, and don't mind
        // deprecation warnings, then uncomment the following block
        /*
         m_thread.suspend();
         */
        // We've paused
        //
        setState(PAUSED);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Override
    public void resume()
        throws Exception {
        // We're resuming
        //
        if (!getState().equals(RESUMING)) {
            setState(RESUMING);
        }

        // Sanity-check--did the Thread fail to initialize?
        //
        if (m_thread == null) {
            return;
        }

        // Set the 'paused' member to false
        //
        m_paused = false;

        // If you prefer a more decisive approach, and don't mind
        // deprecation warnings, then uncomment the following block
        /*
         m_thread.resume();
         */
        // We've started up again
        //
        setState(RESUMING);
    }

    /**
     *
     * @return 
     */
    @Override
    public String getState() {
        return m_state;
    }

    /**
     *
     * @param val
     */
    public void setState(String val) {
        m_state = val;
    }

    /**
     *
     * @return 
     * @throws java.lang.Exception
     */
    @Override
    public String getInstanceID()
        throws Exception {
        return getClass() + ":" + "1.0" + ":"
            + System.currentTimeMillis();
    }

    /**
     *
     * @return 
     */
    public boolean isPaused() {
        return m_paused;
    }

    /**
     *
     * @return 
     */
    public boolean shouldStop() {
        return m_shouldStop;
    }

    /**
     *
     * @param runnable
     */
    public void setRunnable(Runnable runnable)
        throws IllegalThreadStateException {
        if (m_thread != null && m_thread.isAlive()) {
            throw new IllegalThreadStateException();
        }

        m_runnable = runnable;
    }

    /**
     *
     * @param thread
     */
    public void setThread(Thread thread)
        throws IllegalThreadStateException {
        if (m_thread != null && m_thread.isAlive()) {
            throw new IllegalThreadStateException();
        }

        m_thread = thread;
    }

    /**
     *
     * @return 
     */
    public Thread getThread() {
        return m_thread;
    }
}
