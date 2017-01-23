package org.unesco.jisis.dbserver.classloader;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * SocketClassLoader retrieves bytecode for a given class via a
 * HTTP-like protocol. 
 */
public class SocketClassLoader extends ClassLoader
    implements IClassLoaderStrategy
{
    /**
     * Constructor.
     *
     * @param host TCP/IP host name to contact
     * @param port TCP/IP port to contact host on
     */
    public SocketClassLoader(String host, int port)
    {
        this(SocketClassLoader.class.getClassLoader(), host, port);
    }
    /**
     * Constructor.
     *
     * @param host TCP/IP host name to contact
     * @param port TCP/IP port to contact host on
     */
    public SocketClassLoader(ClassLoader parent, 
                             String host, int port)
    {
        // Establish the parent ClassLoader
        //
        super(parent);

        // Store off Socket settings
        //
        m_host = host;
        m_port = port;
    }
    

    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className)
    {
        try
        {
            // Connect to the host on port
            Socket socket = new Socket(m_host, m_port);

            BufferedReader reader =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer =
                new PrintWriter(socket.getOutputStream());
            
            // Send the classname
            writer.println("Classname:" + className);
            writer.flush();
            
            // Get back the resulting bytecode, or get nothing back (an error)
            String line = reader.readLine();
            if (line.equals("Error"))
                return null;
            else if (line.startsWith("Content-Length"))
            {
                // Find out how much we're expecting back
                int colonLoc = line.indexOf(":");
                Integer l = 
                    new Integer(line.substring(colonLoc + 1, line.length()));
                byte[] classBytes = new byte[l.intValue()];
                
                // Throw away any data between our current point in the stream
                // and the first magic number of the Java .class file ('CA')
                while (reader.read() != (int)0xCA)
                    ;

                // We already pulled back the first magic number of the class,
                // so manually insert it into the byte array. Read the rest
                // from the socket
                classBytes[0] = (byte)0xCA;
                for (int i=1; i<classBytes.length; i++)
                    classBytes[i] = (byte)reader.read();

                return classBytes;
            }
            else
                return null;
        }
        catch (UnknownHostException uhEx)
        {
            return null;
        }
        catch (IOException ioEx)
        {
            return null;
        }
    }
    
    /**
     * Return URL for resource given by resourceName
     */
    public URL findResourceURL(String resourceName)
    {
        return null;
    }
    /**
     * Return Enumeration of resources corresponding to
     * resourceName.
     */
    public Enumeration findResourcesEnum(String resourceName)
    {
        return null;
    }
    
    /**
     * Return full path to native library given by the name
     * libraryName.
     */
    public String findLibraryPath(String libraryName)
    {
        return null;
    }

    /**
     * ClassLoader-overridden method to retrive teh bytes
     */
    public Class findClass(String className)
        throws ClassNotFoundException
    {
        byte[] classBytes = findClassBytes(className);
        if (classBytes==null)
        {
            throw new ClassNotFoundException();
        }
                
        return defineClass(className, classBytes, 0, classBytes.length);
    }

    // Internal members    
    //
    String m_host;
    int m_port;
    

    // To test this effectively from the CD, copy 
    // SocketClassLoader.class to a directory elsewhere on your 
    // hard drive. Start GJAS in a separate directory,
    // then run SocketClassLoader from the command-line.    
    public static void main(String[] args)
        throws Exception
    {
        // If Hello.class exists in the current directory, the
        // bootstrap ClassLoader, which is always given first crack,
        // will pick it up and load the class, instead of the
        // SocketClassLoader.
        File file = new File("Hello.class");
        if (file.exists())
            System.out.println("Warning--Hello.class exists " +
                "in the current directory. SocketClassLoader will NOT be used " +
                "to retrieve the file; the primordial ClassLoader will.");

        // Connect to the local host on port 8085 to see if Hello can be
        // loaded.    
        SocketClassLoader scl = new SocketClassLoader("localhost", 8085);
        Class cls = scl.loadClass("Hello");
        Object h = cls.newInstance();
    }
}    