package org.unesco.jisis.dbserver;

//~--- non-JDK imports --------------------------------------------------------


import java.io.File;
import java.util.prefs.Preferences;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;

import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.server.DbServerService;
import org.unesco.jisis.corelib.server.HomeManager;
import org.unesco.jisis.jetty.webserver.JettyRunner;
//import org.unesco.jisis.jisisutils.gui.SwingUtils;


public class JisisDbServer  {
  
    
    private static JettyRunner jettyRunner;
   
    public static Preferences            prefs         = Preferences.userNodeForPackage(JisisDbServer.class);
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JisisDbServer.class);

    private static DbServerService dbServer_ = null;


   
   public boolean start() {

    
       LOGGER.info("Entry in JisisDbServer start()");
       LOGGER.debug(Thread.currentThread().getContextClassLoader().toString());
       if (!isJavaGE17()) {
           LOGGER.error("Java version " + System.getProperty("java.version")
               + "should be >=1.7 cannot start JISIS Database Server");
           return false;
       }

      try {
         LOGGER.info("Creating DbServerService()");
         
         dbServer_ = new DbServerService();
        
         LOGGER.info("Creating Reactor with port: [{}]",dbServer_.getServerPort());
         
         String[] homes = DbServerService.getDbHomeManager().getDbHomeNames();
         String homePath = DbServerService.getDbHomeManager().getDbHomePath(homes[0]);
         
         jettyRunner = new JettyRunner(homePath);

         initServerEnvironment();
         
         initSecurity();



       
         LOGGER.info("Reactor successfully created - Starting a new thread for the server");

//         exec.execute(jisisServer);
         dbServer_.start();
      
         LOGGER.info("Server Thread successfully started");

         /**
          * Should be enable to re-create the users database
          */
        //UserDB.createUserDatabase();
      } catch (Exception ex) {
         LOGGER.error("Exception when starting server", ex);
         return false;
      }
      LOGGER.debug("exiting start()");
      return true;


   }


   public boolean closing() {
     
         return true;
     
   }


  
  public boolean close() {
      try {
          if (dbServer_ != null) {
              dbServer_.stop();
          }
          if (jettyRunner != null) {
              jettyRunner.stop();
          }
      } catch (Exception ex) {
         LOGGER.error("Exception when closing servers",ex);
         return false;
      }
     
      LOGGER.info("Server Thread closed!");
      LOGGER.info("J-ISIS finished");
      return true;
   }


  
    private void initServerEnvironment() {

        Global.setPreference(prefs);

        /* Check that client work dir  exists */
        boolean exists = (new File(getClientWorkPath()).exists());
        if (!exists) {
            System.out.println("Creating Client Work Path: " + getClientWorkPath());
            boolean bmkdir = (new File(getClientWorkPath())).mkdir();
        }
        Global.setClientWorkPath(getClientWorkPath());

        /* Check that client temp dir  exists */
        exists = (new File(getClientTempPath()).exists());
        if (!exists) {
            System.out.println("Creating Client Temp Path: " + getClientTempPath());
            boolean bmkdir = (new File(getClientTempPath())).mkdir();
        }
        Global.setClientTempPath(getClientTempPath());
    }

    /** User's current working directory */
    public static String getHome() {
        return System.getProperty("user.dir");
    }

    public static String getSrvConfigPath() {
        return DbServerService.getSrvConfigPath();
    }

    public static String getDbHomePath() {
        return DbServerService.getDbHomePath();
    }

    public static HomeManager getDbHomeManager() {
        return DbServerService.getDbHomeManager();
    }


      public static String getClientWorkPath() {
        return DbServerService.getJIsisHome() + File.separator + "work" ;
    }

       public static String getClientTempPath() {
        return DbServerService.getJIsisHome() + File.separator + "temp" ;
    }
       
       public static boolean isJavaGE17() {
          boolean b = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) >= 7;
          return b;
       }
       
    public static void initSecurity() {

        //1. Load the INI configuration
        String securityFile = DbServerService.getJIsisHome()+ File.separator + "conf" +  File.separator + "shiro.ini";
        Factory<org.apache.shiro.mgt.SecurityManager> factory
            = new IniSecurityManagerFactory(securityFile);

        //2. Create the SecurityManager
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        //3. Make it accessible
        SecurityUtils.setSecurityManager(securityManager);

    }

}
