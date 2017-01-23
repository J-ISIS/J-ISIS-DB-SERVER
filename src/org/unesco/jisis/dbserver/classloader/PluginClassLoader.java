package org.unesco.jisis.dbserver.classloader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * PluginClassLoader is not an actual ClassLoader, but serves a role
 * of preloading "plugin" classes into the JVM, so that the Plugins
 * can register themselves with whatever "plugin manager" they use.
 *
 * <P>See Chapter 3 of <I>Server-Side Java</I> for a detailed
 * description of how it all works together.
 */
public final class PluginClassLoader
{
    /**
     * Interface to allow interested clients to be notified each
     * time a new plugin class is loaded into the JVM.
     */
    public static interface Listener
    {
        public void pluginLoaded(String pluginName);
        public void exception(Exception ex);
    }

    
    // Private data
    //
    private URLClassLoader urlClassLoader;

    
    /**
     *
     * @param dir
     */
    public PluginClassLoader(String dir)
    {
        this(dir, new Listener()
        {
            @Override
            public void pluginLoaded(String pluginName) { }
            @Override
            public void exception(Exception ex) { }
        });
    }
    /**
     *
     * @param dir
     * @param listener
     */
    public PluginClassLoader(String dir, Listener listener)
    {
        File file = new File(dir);
        reload(file, listener);
    }
    /**
     *
     * @param dir
     */
    public PluginClassLoader(File dir)
    {
        this(dir, new Listener()
        {
            @Override
            public void pluginLoaded(String pluginName) { }
            @Override
            public void exception(Exception ex) { }
        });
    }
    /**
     *
     * @param dir
     * @param listener
     */
    public PluginClassLoader(File dir, Listener listener)
    {
        reload(dir, listener);
    }


    /**
     * Reload the plugins; note that the old URLClassLoader held
     * internally is released, so if the plugin classes loaded
     * earlier aren't in use within the app, they'll get GC'ed.
     *
     * <P><B>HOWEVER</B>, if an instance of an earlier-loaded
     * plugin class is still in existence, it will remain an
     * entirely separate and distinct type from the type loaded
     * in on this plass, even if the .class files are identical!
     * This is because classes loaded into two separate (non-
     * parentally-related) ClassLoaders are considered separate
     * and unrelated types, even if their contents are identical.
     * @param dir
     * @param listener
     */
    public void reload(String dir, Listener listener)
    {
        reload(new File(dir), listener);
    }
    /**
     * Reload the plugins; note that the old URLClassLoader held
     * internally is released, so if the plugin classes loaded
     * earlier aren't in use within the app, they'll get GC'ed.
     *
     * <P><B>HOWEVER</B>, if an instance of an earlier-loaded
     * plugin class is still in existence, it will remain an
     * entirely separate and distinct type from the type loaded
     * in on this plass, even if the .class files are identical!
     * This is because classes loaded into two separate (non-
     * parentally-related) ClassLoaders are considered separate
     * and unrelated types, even if their contents are identical.
     * @param dir
     * @param listener
     */
    public void reload(File dir, Listener listener) {
        String[] contents = getPluginDirContents(dir);

        List<URL> urls = new ArrayList<>();
        List<String> plugins = new ArrayList<>();
        for (String plugin : contents) {
            try {
                File jarFile = new File(dir, plugin);

                Attributes attribs
                        = new JarFile(jarFile).getManifest().getMainAttributes();

                if (attribs.getValue("Plugin-Class") != null) {
                    String pluginClass
                            = attribs.getValue("Plugin-Class");

                    urls.add(jarFile.toURI().toURL());
                    plugins.add(pluginClass.trim());
                    // Need the trim(); getValue() has the
                    // annoying habit of leaving a trailing
                    // space on the end of the class, which will
                    // cause the loadClass() to fail later.
                }
            } catch (IOException ioEx) {
                // Just continue; ignore the file and move on
            } catch (NullPointerException npEx) {
                // No manifest, perhaps?
            }
            //catch (Exception x)
            //{
            //    x.printStackTrace();
            //}
        }

        urlClassLoader
                = URLClassLoader.newInstance(
                        (URL[]) urls.toArray(),
                        getClass().getClassLoader());

        // Preload each of the plugins, giving them the chance to
        // register (in their static initializer block) with whatever
        // "PluginManager" they choose to.
        //
        plugins.forEach((plugin) -> {
            try {
                urlClassLoader.loadClass(plugin).newInstance();
                //urlClassLoader.loadClass(plugin);
                // For some strange reason, just calling
                // loadClass() *won't* actually load the class
                // into the VM; instead, we have to actually
                // *create* an instance of the Plugin class
                // before resolution actually takes place. This
                // would seem to be contrary to the JVM Spec.
                // If this ever changes, then we can comment out
                // the first urlClassLoader.loadClass() line and
                // uncomment the second; the first presumes that
                // the class *can* be instantiated, which is not
                // always a safe/good/acceptable assumption.

                listener.pluginLoaded(plugin);
            } catch (Exception ex) {
                listener.exception(ex);
            }
        });
    }


    /**
     * Releases the handle on the URLClassLoader used internally;
     * this will have the effect of allowing all the plugin classes,
     * if not referenced anywhere else within the application, to be
     * GC'ed the next time GC takes place.
     */
    public void unload() {
        urlClassLoader = null;
    }


    /**
     * Returns a String array of filenames in the directory which are
     * potential plugin files.
     *
     * @param dir The File object representing the directory to iterate
     *            through
     */
    private String[] getPluginDirContents(File dir) {
        // Sanity check--does the directory exist?
        if ((!dir.exists())
                || (!dir.isDirectory())) {
            return new String[0];
        }

        String[] contents  = dir.list((dir1, name) -> {
            return name.endsWith(".jar") || name.endsWith(".zip");
        } );

        
        
        return contents;
    }
    /**
     * Returns a String array of filenames in the directory which are
     * .class files.
     *
     * @param dir The File object representing the directory to iterate
     *            through
     */
    private String[] getPluginDirClasses(File dir)
    {
        String[] contents = dir.list((dir1, name) -> 
             name.endsWith(".class"));
        
        return contents;
    }
    /**
   


    /**
     * Test suite--just load whatever plugins happen to be in the
     * current directory.
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        PluginClassLoader pcl = 
            new PluginClassLoader(".", new Listener () 
            {
                @Override
                public void pluginLoaded(String pluginName)
                {
                    System.out.println(pluginName + " loaded.");
                }
                @Override
                public void exception(Exception ex)
                {
                    System.out.println("Exception:");
                }
            });
    }
}
