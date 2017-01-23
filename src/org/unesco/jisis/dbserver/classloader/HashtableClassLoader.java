package org.unesco.jisis.dbserver.classloader;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

class ByteArray implements java.io.Serializable {
    
      private byte[] bytes_;

    public ByteArray(byte[] bytes) {
        bytes_ = bytes;
    }

    public byte[] getBytes() {
        return bytes_;
    }

  
}

/**
 * HashtableClassLoader
 */
public class HashtableClassLoader extends java.lang.ClassLoader implements IClassLoaderStrategy {

      // Internal members
    //
    private Map<String, ByteArray> classTable_;

    /**
     *
     */
    public HashtableClassLoader() {
        this(HashtableClassLoader.class.getClassLoader());
    }

    /**
     *
     * @param table
     */
    public HashtableClassLoader(Map<String, ByteArray> table) {
        this(HashtableClassLoader.class.getClassLoader(), table);
    }

    /**
     *
     * @param parent
     */
    public HashtableClassLoader(ClassLoader parent) {
        this(parent, new HashMap<String, ByteArray>());
    }

    /**
     *
     * @param parent
     * @param table
     */
    public HashtableClassLoader(ClassLoader parent, Map<String, ByteArray> table) {
        super(parent);

        classTable_ = table;
    }

    /**
     *
     * @param className
     * @param bytes
     */
    public void putClass(String className, byte[] bytes) {
        classTable_.put(className, new ByteArray(bytes));
    }

    /**
     * Return byte array (which will be turned into a Class instance via
     * ClassLoader.defineClass) for class
     * @param className
     * @return 
     */
    @Override
    public byte[] findClassBytes(String className) {
        try {
            ByteArray byteArray = classTable_.get(className);
            byte[] bytes = byteArray.getBytes();
            return bytes;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Return URL for resource given by resourceName
     * @param resourceName
     * @return 
     */
    @Override
    public URL findResourceURL(String resourceName) {
        return null;
    }

    /**
     * Return Enumeration of resources corresponding to resourceName.
     * @param resourceName
     * @return 
     */
    @Override
    public Enumeration findResourcesEnum(String resourceName) {
        return null;
    }

    /**
     * Return full path to native library given by the name libraryName.
     * @param libraryName
     * @return 
     */
    @Override
    public String findLibraryPath(String libraryName) {
        return null;
    }

    /**
     *
     * @param className
     * @return 
     * @throws java.lang.ClassNotFoundException 
     */
    @Override
    public Class findClass(String className)
            throws ClassNotFoundException {
        byte[] bytes = findClassBytes(className);
        if (bytes == null) {
            throw new ClassNotFoundException();
        }

        return defineClass(className, bytes, 0, bytes.length);
    }

  
    // Driver
    //
    public static void main(String[] args)
            throws Exception {
        // Try the HashtableClassLoader
        HashtableClassLoader hcl = new HashtableClassLoader();

        // Load "Hello.class" from root dir into the Hashtable
        FileInputStream fis = new FileInputStream("/Hello.class");
        int ct = fis.available();
        byte[] Hello_bytes = new byte[ct];
        fis.read(Hello_bytes);

        hcl.putClass("Hello", Hello_bytes);

        // Try the loadClass
        Object obj = hcl.loadClass("Hello").newInstance();
    }
}
