package org.unesco.jisis.dbserver.classloader;

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;

/**
 * ClassLoaderStrategy provides a Strategy pattern interface
 * for Java 2's ClassLoader scheme. 
 */
public interface IClassLoaderStrategy
    extends Serializable
{
    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className);
    
    /**
     * Return URL for resource given by resourceName
     */
    public URL findResourceURL(String resourceName);
    /**
     * Return Enumeration of resources corresponding to
     * resourceName.
     */
    public Enumeration findResourcesEnum(String resourceName);
    
    /**
     * Return full path to native library given by the name
     * libraryName.
     */
    public String findLibraryPath(String libraryName);
}    