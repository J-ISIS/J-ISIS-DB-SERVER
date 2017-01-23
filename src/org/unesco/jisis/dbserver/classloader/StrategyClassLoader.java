package org.unesco.jisis.dbserver.classloader;

import java.net.URL;
import java.util.Enumeration;

/**
 * StrategyClassLoader uses the Strategy pattern to implement
 * more reusable (and chainable) ClassLoader-possibilities.
 *
 * <P>Note that the StrategyClassLoader itself in turn implements
 * the ClassLoaderStrategy interface; this means that instances
 * of the StrategyClassLoader could, in turn, be used within a
 * StrategyClassLoader. The need for this isn't obvious until
 * one considers the CompositeClassLoader, which uses a collection
 * of ClassLoaderStrategy-implementing classes to do its work.
 * Without implementing this interface, StrategyClassLoader would
 * be unable to participate in that system.
 */
public class StrategyClassLoader extends ClassLoader
    implements IClassLoaderStrategy
{
    // Internal members
    //
    private IClassLoaderStrategy m_strategy =
        new IClassLoaderStrategy()
    {
        public byte[] findClassBytes(String className)
        {
            return null;
        }
        public URL findResourceURL(String resourceName)
        {
            return null;
        }
        public Enumeration findResourcesEnum(String resourceName)
        {
            return null;
        }
        public String findLibraryPath(String libraryName)
        {
            return null;
        }
    };
        // This default ClassLoaderStrategy is a NullObject
        // implementation of ClassLoaderStrategy; this way, if the
        // StrategyClassLoader is created without a Strategy object,
        // StrategyClassLoader's semantics are preserved without
        // having to do explicit "null" checks.
    

    /**
     * Construct using the given Strategy instance and use the
     * ClassLoader that loaded this class as the parent.
     */
    public StrategyClassLoader(IClassLoaderStrategy strategy)
    {
        this(strategy, StrategyClassLoader.class.getClassLoader());
    }
    /**
     * Construct using the given Strategy instance and use the
     * ClassLoader passed in as the parent.
     */
    public StrategyClassLoader(IClassLoaderStrategy strategy,
                               ClassLoader parent)
    {
        super(parent);
        
        if (strategy != null)
            m_strategy = strategy;
    }


    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className)
    {
        return m_strategy.findClassBytes(className);
    }
    
    /**
     * Return URL for resource given by resourceName
     */
    public URL findResourceURL(String resourceName)
    {
        return m_strategy.findResourceURL(resourceName);
    }
    /**
     * Return Enumeration of resources corresponding to
     * resourceName.
     */
    public Enumeration findResourcesEnum(String resourceName)
    {
        return m_strategy.findResourcesEnum(resourceName);
    }
    
    /**
     * Return full path to native library given by the name
     * libraryName.
     */
    public String findLibraryPath(String libraryName)
    {
        return m_strategy.findLibraryPath(libraryName);
    }

    
    
    /**
     * Find the class bytecode; defers to the Strategy's
     * <CODE>findClassBytes</CODE> method.
     */
    protected Class findClass(String name)
        throws ClassNotFoundException
    {
        byte[] classBytes = findClassBytes(name);
        
        if (classBytes == null)
        {
            throw new ClassNotFoundException();
        }
        
        return defineClass(name, classBytes, 0, classBytes.length);
    }
    
    
    /**
     * For JDK1.1 use, uncomment the following method
     */
    /*
    public synchronized Class loadClass()
    {
    }
    */
    
    
    // Test driver
    //
    public static void main(String[] args)
        throws Exception
    {
        // Create an anonymous Strategy to use for this
        // test alone
        IClassLoaderStrategy strat = new IClassLoaderStrategy()
        {
            public byte[] findClassBytes(String className)
            {
                // Load "Hello.class" from root dir
                try
                {
                    java.io.FileInputStream fis = 
                        new java.io.FileInputStream("/Hello.class");
                    int ct = fis.available();
                    byte[] Hello_bytes = new byte[ct];
                    fis.read(Hello_bytes);
                    
                    return Hello_bytes;
                }
                catch (Exception ex)
                {
                    return null;
                }
            }
            
            public java.net.URL 
                findResourceURL(String resourceName)
            {
                return null;
            }
            public java.util.Enumeration 
                findResourcesEnum(String resName)
            {
                return null;
            }
            
            public String findLibraryPath(String libraryName)
            {
                return "";
            }
        };
        
        StrategyClassLoader scl = new StrategyClassLoader(strat);
        
        Object obj = scl.loadClass("Hello").newInstance();
    }
}    