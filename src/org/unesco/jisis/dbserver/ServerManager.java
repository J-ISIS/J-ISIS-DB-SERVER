/*
 * ServerManager.java
 *
 * Created on 25 septembre 2007, 18:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.dbserver;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.unesco.jisis.corelib.server.ConfigProperties;
import org.unesco.jisis.corelib.server.IService;
import org.unesco.jisis.dbserver.classloader.IClassLoaderStrategy;


/**
 * The core is the ServerManager class, which is the shell in which user-defined
 * Service-derived classes will execute. ServerManager, more than any other class in the
 * JAS system, is the heart and soul of its application server. It holds the Services added
 * to it, hands out references to Services as requested by clients, and provides the basic
 * backplane for the GJAS system. However, in order to provide the maximum amount of
 * location transparency, we don’t necessarily want the ServerManager class itself doing
 * the actual work—we’d like to be able to connect with ServerManagers in other virtual
 * machines, and so forth. We’ll get to that later, but we’ll lay the groundwork now.
 * We create ServerManager in a separated fashion. First, we’ll create the basic
 * ServerManager interface, called IServerManager, that any class wishing to provide
 * ServerManager-like behavior must implement. Next, we’ll create a class, ServerManager,
 * that provides static-level access to the IServerManager instance. We want
 * only one ServerManager instance in a given JVM (the classic Singleton pattern), but
 * we don’t know ahead of time which we want. The reason we go to all this trouble is
 * that we want to provide a single way of interacting with the ServerManager, but we
 * want to vary the actual ServerManager implementation used. We write the ServerManager
 * class to check to ensure that only one IServerManager instance is ever set as the
 * instance, and require any IServerManager-implementing class to set itself
 *  
 */

public class ServerManager {
   // Internal data
   //

   private static IServerManager s_instance;

   /**
    * Get the IServerManager instance for this JVM; may return null
    * if one hasn't been designated yet.
     * @return 
    */
   public static IServerManager instance() {
      return s_instance;
   }

   /**
    * Set the IServerManager instance for this JVM; note that only
    * <B>one</B> IServerManager per JVM is allowed.
    */
   static IServerManager instance(IServerManager svrMgr) {
      if (ServerManager.instance() != null) {
         throw new RuntimeException("Only one ServerManager" +
                 " instance permitted within any JVM.");
      }

      s_instance = svrMgr;
      return s_instance;
   }

   /**
    * Parse an InputStream for the Service name to load and add to
    * the ServerManager instance.
     * @param in
     * @throws java.io.IOException
    */
   public static void parseInputStream(InputStream in)
           throws IOException {
      BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
      StreamTokenizer st = new StreamTokenizer(rdr);

      st.commentChar(';');        // semicolons are single-line arguments
      st.eolIsSignificant(true);  // end-of-line is important
      st.slashSlashComments(true);// allow C++ '//' comments
      st.slashStarComments(true); // allow C '/*'-'*/' comments

      List<String> lines = new ArrayList<>();
      while (st.nextToken() != StreamTokenizer.TT_EOF) {
         String line = new String();
          switch (st.ttype) {
              case StreamTokenizer.TT_EOL:
                  log("Parsed line: " + line);
                  lines.add(line);
                  line = new String();
                  break;
              case '"':
                  line += '"';
                  break;
              default:
                  line += st.sval;
                  break;
          }
      }
   /*
   while (st.nextToken() != StreamTokenizer.TT_EOF)
   {
   // Check to make sure the token isn't an immediate TT_EOL
   //
   if (st.ttype == StreamTokenizer.TT_EOL)
   continue;

   // Get the classname of the Service to load
   //
   String clsname = st.sval;
   if (clsname == null)
   continue;

   // Successive tokens on the same line are arguments to the
   // Service's start() call
   //
   Vector argsVector = new Vector();
   while (true)
   {
   // Break out of gathering arguments on either end-of-line
   // or end-of-file
   //
   st.nextToken();

   if (st.ttype == StreamTokenizer.TT_EOL ||
   st.ttype == StreamTokenizer.TT_EOF)
   break;

   // Check to see if the token is a number; StreamTokenizer
   // won't, strangely enough, convert the number into a
   // String in st.sval for you--you have to do it yourself.
   if (st.ttype == StreamTokenizer.TT_NUMBER)
   {
   // StreamTokenizer returns numbers as doubles;
   // this means if we do a toString() on it,
   // integer values will come back with ".0" tacked
   // on the end.
   Double dbl = new Double(st.nval);
   String srep;
   if (dbl.doubleValue() == dbl.longValue())
   {
   srep = Long.toString(dbl.longValue());
   }
   else
   {
   srep = dbl.toString();
   }

   log("Extracting token: " + srep);
   argsVector.addElement(srep);
   }
   else if (st.sval != null)
   {
   log("Extracting token: " + st.sval);
   argsVector.addElement(new String(st.sval));
   }
   }

   // Copy them over to an array out of the Vector
   //

   // Load the Service
   //
   log("ServerManager.loadDirectives(): Loading " + clsname);
   addServiceFromLocal(clsname, config);
   }
    */
   }

   /**
    * Parse a single line for a classname arg0 arg1 ... argN
    * sequence, and add the Service to the ServerManager if
    * everything parses successfully.
     * @param arg
    */
   public static void parseArg(String arg) {
      String classname;
      try {
         // Assume that it's a class we're to load; parse it
         // (since arguments to the class may come inside of
         // the quoted argument) and try to load it
         //
         StringTokenizer st = new StringTokenizer(arg);

         // First token should be the classname
         classname = st.nextToken();
         if (classname == null) {
            return;
         }

         // Check for additional tokens, convert them to Strings
         List<String> arguments = new ArrayList<>();
         while (st.hasMoreElements()) {
            String token = st.nextToken();
            arguments.add(token);
         }

         // At this point, classname holds our class to load,
         // and argsVector holds the Vector of args to pass;
         // convert argsVector into an array, then load the Service,
         // and add it into the ServerManager (us)
         Properties config = new Properties();
         arguments.forEach((argString) -> {
             if (argString.startsWith("-")) {
                 String argName = argString.substring(
                         argString.indexOf("-") + 1,
                         argString.indexOf(":"));
                 String argValue = argString.substring(
                         argString.indexOf(":") + 1,
                         argString.length());
                 
                 ServerManager.log(
                         "argName = '" + argName + "' = '" +
                                 argValue + "'");
                 
                 config.setProperty(argName, argValue);
             } else if (argString.startsWith("/")) {
                 String argName = argString.substring(
                         argString.indexOf("/") + 1,
                         argString.indexOf(":"));
                 String argValue = argString.substring(
                         argString.indexOf(":") + 1,
                         argString.length());
                 
                 ServerManager.log(
                         "argName = '" + argName + "' = '" +
                                 argValue + "'");
                 
                 config.setProperty(argName, argValue);
             }
          });

         //addServiceFromLocal(classname, config);
         addServiceFromLocal(classname, new ConfigProperties());
      } catch (Exception ex) {
         // ERROR! Can't load
         ServerManager.error(ex);
      }
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
    */
   public static void shutdown() {
      s_instance.shutdown();
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param serviceName
     * @param strategy
    */
   public static void deployService(String serviceName,
                                    IClassLoaderStrategy strategy) {
      s_instance.deployService(serviceName, strategy);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param svcName
     * @return 
    */
   public static IServer loadService(String svcName) {
      return s_instance.loadService(svcName);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param svc
     * @return 
    */
   public static IServer loadService(IService svc) {
      return s_instance.loadService(svc);
   }

   /**
    *
     * @param svcName
     * @param args
     * @return 
    */
   public static IServer addService(String svcName,
                                    ConfigProperties args) {
      return s_instance.addService(svcName, args);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param svc
     * @param args
     * @return 
    */
   public static IServer addService(IService svc, ConfigProperties args) {
      return s_instance.addService(svc, args);
   }

   /**
    * This method loads the Service from the local ClassLoader
    * (probably the bootstrap ClassLoader) and passes that to
    * the IServerManager singleton instance.
     * @param svcName
     * @param args
     * @return 
    */
   public static IServer addServiceFromLocal(String svcName,
           ConfigProperties args) {
      try {
         IService svc = (IService) Class.forName(svcName).newInstance();
         return addService(svc, args);
      } catch (Exception ex) {
         error(ex);
         return null;
      }
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param instanceID
    */
   public static void removeService(String instanceID) {
      s_instance.removeService(instanceID);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param instanceID
    */
   public static void killService(String instanceID) {
      s_instance.killService(instanceID);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @return 
    */
   public static String[] getServices() {
      return s_instance.getServices();
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param instanceID
     * @return 
    */
   public static IServer getService(String instanceID) {
      return s_instance.getService(instanceID);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param msg
    */
   public static void log(String msg) {
      s_instance.log(msg);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param ex
    */
   public static void log(Exception ex) {
      s_instance.log(ex);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param msg
    */
   public static void error(String msg) {
      s_instance.error(msg);
   }

   /**
    * Call the method of the same name on the IServerManager
    * Singleton instance.
     * @param ex
    */
   public static void error(Exception ex) {
      s_instance.error(ex);
   }

   private ConfigProperties argsVectorToProperties(List<String> arguments) {
      Properties config = new Properties();

      arguments.forEach((argument) -> {
          if (argument.startsWith("-")) {
              String argName = argument.substring(argument.indexOf("-") + 1,
                      argument.indexOf(":"));
              String argValue = argument.substring(argument.indexOf(":") + 1,
                      argument.length());
              
              ServerManager.log(
                      "'" + argName + "' = '" +
                              argValue + "'");
              
              config.setProperty(argName, argValue);
          } else if (argument.startsWith("/")) {
              String argName = argument.substring(argument.indexOf("/") + 1,
                      argument.indexOf(":"));
              String argValue = argument.substring(argument.indexOf(":") + 1,
                      argument.length());
              
              ServerManager.log(
                      "argName = '" + argName + "' = '" +
                              argValue + "'");
              
              config.setProperty(argName, argValue);
          } else if (argument.startsWith("@")) {
          }
       });

      return new ConfigProperties();
   }
}       