package org.unesco.jisis.dbserver.classloader;

import java.io.*;
import java.net.URL;

public class FileSystemClassLoader extends ClassLoader
    implements IClassLoaderStrategy
{
    /**
     * Default constructor uses the home directory of the JDK as its
     * root in the filesystem.
     */
    public FileSystemClassLoader()
        throws FileNotFoundException
    {
        this(FileSystemClassLoader.class.getClassLoader(),
             System.getProperties().getProperty("java.home"));
    }
    /**
     * Constructor taking a String indicating the point on the local
     * filesystem to take as the root in the filesystem.
     */
    public FileSystemClassLoader(String root)
        throws FileNotFoundException
    {
        this(FileSystemClassLoader.class.getClassLoader(),
             root);
    }
    /**
     * Default constructor uses the home directory of the JDK as its
     * root in the filesystem.
     */
    public FileSystemClassLoader(ClassLoader parent)
        throws FileNotFoundException
    {
        this(parent, 
             System.getProperties().getProperty("java.home"));
    }
    /**
     * Constructor taking a String indicating the point on the local
     * filesystem to take as the root in the filesystem.
     */
    public FileSystemClassLoader(ClassLoader parent, String root)
        throws FileNotFoundException
    {
        // Ensure we defer to parent appropriately
        //
        super(parent);

        // Test to make sure root is a legitimate directory on the
        // local filesystem
        //
        File f = new File(root);
        if (f.isDirectory())
            m_root = root;
        else
            throw new FileNotFoundException();
    }
    

    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className)
    {
        try
        {
            // Assume that 'name' follows standard Java 
            // package-to-directory naming conventions, where each
            // "." represents a directory separator character 
            // (backslash on Windows, slash on Unix, colon on Mac).
            //
            String pathName = m_root + File.separatorChar +
                className.replace('.', File.separatorChar) + 
                ".class";
               
            // Try to open the file and read in its contents
            //
            FileInputStream inFile =
                new FileInputStream(pathName);
            byte[] classBytes = new byte[inFile.available()];
            inFile.read(classBytes);

            return classBytes;
        }
        catch (java.io.IOException ioEx)
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
    public java.util.Enumeration findResourcesEnum(String resourceName)
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
     * Attempt to find the bytecode given for the class <code>name</code>
     * from a file on disk. Will not look along CLASSPATH, nor in .jar
     * files
     */
    public Class findClass(String name)
        throws ClassNotFoundException
    {
        byte[] classBytes = findClassBytes(name);
        
        if (classBytes==null)
        {
            throw new ClassNotFoundException();
        }
        else
        {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
    }
    
    
    private String m_root = null;
    
    
    // Test driver
    //
    public static void main(String[] args)
        throws Exception
    {
        String userDir = 
            System.getProperties().getProperty("user.dir");
        FileSystemClassLoader fscl = 
            new FileSystemClassLoader(userDir);
        
        if (args.length > 0)
        {
            Class c = fscl.loadClass(args[0]);
            Object o = c.newInstance();
            System.out.println(o.getClass().getName());
        }
        else
        {
            // Test the ClassLoader by trying to load itself! (I 
            // first found the idea in "Java Virtual Machine", by
            // Troy Downing and Jon Meyer (O'Reilly), who in turn
            // credit the URL
            // http://magma.Mines.edu/students/d/drferrin/Cool_Beans.)
            //
            Class c = fscl.loadClass(
                "com.javageeks.classloader.FileSystemClassLoader");
            
            // Instantiate an instance of the FileSystemClassLoader
            // as an Object; leave it like this for the moment
            //
            Object o = c.newInstance();
            
            // Verify that it is, in fact, a FileSystemClassLoader
            //
            System.out.println(o.getClass().getName());
            
            // Note--because of the Java namespaces mechanism, this
            // cast will fail! This is because FileSystemClassLoader
            // was first loaded by the primordial ClassLoader, and
            // the attempt to cast the new Object (which was
            // returned by the FileSystemClassLoader we
            // created a few lines ago) will fail; you cannot cast
            // across ClassLoader lines.
            //
            FileSystemClassLoader fscl2 = 
                (FileSystemClassLoader)c.newInstance();
        }
    }
}    