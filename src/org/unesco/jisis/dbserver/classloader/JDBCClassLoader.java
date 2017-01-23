package org.unesco.jisis.dbserver.classloader;

import java.net.URL;
import java.sql.*;
import java.util.Enumeration;

public class JDBCClassLoader extends ClassLoader
    implements IClassLoaderStrategy
{
    /**
     * Constructor.
     *
     * The SQL statement must return at least one row, the first
     * column of which will be a BINARY column, and must contain a
     * ? where the name of the fully-qualified classname will appear.
     * Example:
     * "SELECT bytecode FROM class_tbl WHERE class_tbl.name = ?"
     *
     * @param conn The JDBC Connection to use. Must be already
     *   connected.
     * @param sql The SQL statement to execute to retrieve the
     *   bytecode.
     */
    public JDBCClassLoader(Connection conn, String sql)
    {
        this(JDBCClassLoader.class.getClassLoader(), conn, sql);
    }
    /**
     * Constructor.
     *
     * The SQL statement must return at least one row, the first
     * column of which will be a BINARY column, and must contain a
     * ? where the name of the fully-qualified classname will appear.
     * Example:
     * "SELECT bytecode FROM class_tbl WHERE class_tbl.name = ?"
     *
     * @param parent The parent ClassLoader (in the 1.2 JDK scheme 
     *   of things)
     * @param conn The JDBC Connection to use. Must be already
     *   connected.
     * @param sql The SQL statement to execute to retrieve the
     *   bytecode.
     */
    public JDBCClassLoader(ClassLoader parent, 
                           Connection conn, String sql)
    {
        // Set parent ClassLoader
        //
        super(parent);

        // Store the JDBC settings
        //
        m_connection = conn;
        m_sql = new String(sql);
    }


    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className)
    {
        byte[] classBytes = retrieveClass(className);
        return classBytes;
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
     * Called by ClassLoader.loadClass when a classname is requested.
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

    /**
     * Internal method to do the actual SQL-retrieval of the bytecode
     */    
    private byte[] retrieveClass(String className)
    {
        try
        {
            // Create a SQL Statement
            Statement stmt = null;
            stmt = m_connection.createStatement();
            
            // Build our SQL statement
            String pre = m_sql.substring(0, m_sql.indexOf("?"));
            String post = m_sql.substring(m_sql.indexOf("?")+1, m_sql.length());
            String sql = pre + className + post;
            
            // Do the query
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                byte[] bytes = rs.getBytes(1);
                return bytes;
            }
            else
                return null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    // Internal members
    //    
    private Connection m_connection;
    private String m_sql;
    

    /**
     * Test driver routine; assumes an IDB database with the following
     * schema:<BR>
     * CREATE TABLE class_tbl (
     *   bytecode binary,
     *   classname varchar(80) primary key
     * );
     */    
    public static void main(String[] args)
        throws Exception
    {
        // Load the IDB driver
        Class.forName("jdbc.idbDriver").newInstance();

        // Do the JDBC Connection
        java.util.Properties p = new java.util.Properties();
        Connection conn =
            DriverManager.getConnection("jdbc:idb:sample.prp", p);

        // Create the ClassLoader around the SQL statement to
        // retrieve the bytecode
        JDBCClassLoader jdbcClassLoader = 
            new JDBCClassLoader(conn, 
                "SELECT bytecode FROM class_tbl " +
                "WHERE classname = '?'");
        
        Class cls = jdbcClassLoader.loadClass("Hello");
        Object h = cls.newInstance();
            // Should print "Hello, world!"
    }
}    