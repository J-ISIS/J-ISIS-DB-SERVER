package org.unesco.jisis.dbserver.classloader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 *
 */
public class CompositeClassLoader extends ClassLoader
        implements IClassLoaderStrategy {
    // Internal members
    //

    private final List<IClassLoaderStrategy> loaders_ = new ArrayList<>();

    /**
     *
     */
    public CompositeClassLoader() {
        this(CompositeClassLoader.class.getClassLoader(), null);
    }

    /**
     *
     * @param loaders
     */
    public CompositeClassLoader(IClassLoaderStrategy[] loaders) {
        this(CompositeClassLoader.class.getClassLoader(), loaders);
    }

    /**
     *
     * @param parent
     */
    public CompositeClassLoader(ClassLoader parent) {
        this(parent, null);
    }

    /**
     *
     * @param parent
     * @param loaders
     */
    public CompositeClassLoader(ClassLoader parent,
            IClassLoaderStrategy[] loaders) {
        // Establish parent ClassLoader relationship
        //
        super(CompositeClassLoader.class.getClassLoader());

        // Copy over ClassLoaderStrategy instances (if any)
        //
        if (loaders != null && loaders.length > 0) {
            loaders_.addAll(Arrays.asList(loaders));
        }
    }

    /**
     *
     * @param cls
     */
    public void addLoader(IClassLoaderStrategy cls) {
        loaders_.add(cls);
    }

    /**
     *
     * /**
     *
     * @param cls
     */
    public void removeLoader(IClassLoaderStrategy cls) {
        loaders_.remove(cls);
    }

    /**
     * Return byte array (which will be turned into a Class instance via
     * ClassLoader.defineClass) for class
     *
     * @param className
     * @return
     */
    @Override
    public byte[] findClassBytes(String className) {
        byte[] bytecode = null;

        for (IClassLoaderStrategy strat : loaders_) {

            bytecode = strat.findClassBytes(className);

            if (bytecode != null) {
                return bytecode;
            }
        }

        return bytecode;
    }

    /**
     * Return URL for resource given by resourceName
     *
     * @param resourceName
     * @return
     */
    @Override
    public URL findResourceURL(String resourceName) {
        URL resource = null;

        for (IClassLoaderStrategy strat : loaders_) {

            resource = strat.findResourceURL(resourceName);

            if (resource != null) {
                return resource;
            }
        }

        return resource;
    }

    /**
     * Return Enumeration of resources corresponding to resourceName.
     *
     * @param resourceName
     * @return
     */
    @Override
    public Enumeration findResourcesEnum(String resourceName) {
        Enumeration resourceEnum = null;

        for (IClassLoaderStrategy strat : loaders_) {

            resourceEnum = strat.findResourcesEnum(resourceName);

            if (resourceEnum != null) {
                return resourceEnum;
            }
        }

        return resourceEnum;
    }

    /**
     * Return full path to native library given by the name libraryName.
     *
     * @param libraryName
     * @return
     */
    @Override
    public String findLibraryPath(String libraryName) {
        String libPath = null;

        for (IClassLoaderStrategy strat : loaders_) {

            libPath = strat.findLibraryPath(libraryName);

            if (libPath != null) {
                return libPath;
            }
        }

        return libPath;
    }

    /**
     * Find the class bytecode; defers to the Strategy's
     * <CODE>findClassBytes</CODE> method.
     *
     * @param name
     * @return
     * @throws java.lang.ClassNotFoundException
     */
    @Override
    protected Class findClass(String name)
            throws ClassNotFoundException {
        byte[] classBytes = findClassBytes(name);

        if (classBytes == null) {
            throw new ClassNotFoundException();
        }

        return defineClass(name, classBytes, 0, classBytes.length);
    }

    // Test driver
    //
    public static void main(String[] args)
            throws Exception {
        // Build the array of Strategy instances
        IClassLoaderStrategy classLoaderArray[]
                = new IClassLoaderStrategy[2];

        // Build a FileSystemClassLoader
        classLoaderArray[0]
                = new FileSystemClassLoader(
                        System.getProperties().getProperty("testing.dir"));

        // Build a HashtableClassLoader
        classLoaderArray[1] = new HashtableClassLoader();
        FileInputStream fis = new FileInputStream("/Hello.class");
        int ct = fis.available();
        byte[] Hello_bytes = new byte[ct];
        fis.read(Hello_bytes);
        ((HashtableClassLoader) classLoaderArray[1]).putClass("Hello", Hello_bytes);

        // Build the CompositeClassLoader made up of those two
        CompositeClassLoader ccl
                = new CompositeClassLoader(classLoaderArray);

        // Load one from the HashtableClassLoader
        Object o1 = ccl.loadClass("Hello").newInstance();

        // Load one from the FileSystemClassLoader (cmd-line param)
        if (args.length > 0) {
            Object o2 = ccl.loadClass(args[0]).newInstance();
        }
    }
}
