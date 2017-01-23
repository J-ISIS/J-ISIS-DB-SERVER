package org.unesco.jisis.dbserver.classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * CompilerClassLoader
 *
 * <P><B>Note:</B>If this class fails to compile with the error
 * message "Class sun.tools.javac.Main not found", then you need
 * to put the "tools.jar" file on the CLASSPATH either in the
 * environment, or explicitly on the command-line to the
 * compiler:
 * <PRE>
 * javac -classpath $(JDKROOT)/lib/tools.jar <.java files>
 * </PRE>
 * <P>Dropping the tools.jar file into the Extensions directory
 * is discouraged, as some tools (like RMI-IIOP's rmic.exe) will
 * have problems picking out the right Java classes to execute,
 * since the Extensions are always ahead of any other classes on
 * a CLASSPATH.
 */
public class CompilerClassLoader extends java.lang.ClassLoader
    implements IClassLoaderStrategy
{
    /**
     * Uses "user.home" as root dir to work from
     */
    public CompilerClassLoader()
    {
        this(CompilerClassLoader.class.getClassLoader());
    }
    /**
     *
     */
    public CompilerClassLoader(File sourceDirRoot)
    {
        this(sourceDirRoot, 
             CompilerClassLoader.class.getClassLoader());
    }
    /**
     *
     */
    public CompilerClassLoader(ClassLoader parent)
    {
        // Pass up our parent ClassLoader
        //
        super(parent);

        // Get our source "root" directory
        //
        try
        {
            m_sourceDirRoot = 
                new File(System.getProperty("user.home"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            m_sourceDirRoot = null;
        }
    }
    /**
     *
     */
    public CompilerClassLoader(File sourceDirRoot, 
                               ClassLoader parent)
    {
        // Pass up our parent ClassLoader
        //
        super(parent);
        
        // Get our source "root" directory
        //
        m_sourceDirRoot = sourceDirRoot;
    }
    
    
    /**
     *
     */
    public String getClasspath()
    {
        return m_classpath;
    }
    /**
     *
     */
    public void setClasspath(String classpath)
    {
        m_classpath = classpath;
    }


    
    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String name)
    {
        if (m_sourceDirRoot == null)
            return null;
            
        // Translate the Java-canonical name into an equivalent
        // file name; anything after a "$" is removed, since "$"
        // only shows up in anonymous/inner classes, which are
        // from the "$"-prefixed file. Tack a .java on it, and
        // look for the file
        String javaName = name;
        if (javaName.indexOf("$") > 0)
            javaName = javaName.substring(0, javaName.indexOf("$"));
        // Replace "." with File.fileSeparatorChar's
        javaName = javaName.replace('.', File.separatorChar);
        javaName += ".java";
        
        File javaFile = new File(m_sourceDirRoot, javaName);
        //System.out.println("Looking for " + javaFile.toString());
        
        // Attempt to compile it down to bytecode
		String[] javacArgs = new String[]
        {
    		//"-classpath",
    		//m_classpath,
    		"-deprecation",
    		javaFile.getPath()
		};
		ByteArrayOutputStream javacOut = new ByteArrayOutputStream();
//		sun.tools.javac.Main javaCompiler =
//            new sun.tools.javac.Main(
//                new PrintStream(javacOut, true), "javac");
//		if(!javaCompiler.compile(javacArgs))
//        {
//            return null;
//		}

        // If we got here, the file compiled just fine; load its
        // bytecode into the byte array
        String className = null;
        if (name.lastIndexOf("$") > -1)
        {
            className = name.replace('.', File.separatorChar)
                + ".class";
        }
        else
        {
            className = javaName.substring(0, 
                javaName.lastIndexOf(".")) + ".class";
        }
        
        try
        {
            File inFile =
                new File(m_sourceDirRoot, className);
            FileInputStream in = 
                new FileInputStream(inFile);
                
            byte[] bytecode = new byte[(int)inFile.length()];
            in.read(bytecode, 0, (int)inFile.length());
            
            // Return the bytecode
            return bytecode;
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
     * Retrieve compiled code
     */    
    protected Class findClass(String name)
        throws ClassNotFoundException
    {
        byte[] bytecode = findClassBytes(name);
        if (bytecode == null)
        {
            throw new ClassNotFoundException();
        }

        return defineClass(name, bytecode, 0, bytecode.length);
    }
    
    
    // Internal members
    private File m_sourceDirRoot;
    private String m_classpath;
    
    
    // Test driver
    public static void main(String[] args)
        throws Exception
    {
        CompilerClassLoader cl = 
            new CompilerClassLoader(new File("C:\\"));
        cl.loadClass("Test.PkgHello").newInstance();
    }
}        