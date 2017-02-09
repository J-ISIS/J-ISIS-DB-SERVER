/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dbserver;

/**
 *
 * @author jc dauphin
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      try {
         
         
         ConsoleControlServer console = new ConsoleControlServer();
         
         if (null != args && null != args[0]){
             console.setStartParam(args[0]);
         }
         
         console.start();
         console.run();
         console.kill();
      } catch (Exception ex) {
         ex.printStackTrace();
         //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

}
