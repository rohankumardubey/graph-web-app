package org.thobe.neo.graphapp.packaging;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

/**
 * Not safe for multi-threaded use.
 */
public class UberJar
{
    public static final String SERVICES_PREFIX = "META-INF/services/";
    private final byte[] buffer = new byte[4096];

    private final Manifest manifest = new Manifest();

    {
        manifest.getMainAttributes().put( Attributes.Name.MANIFEST_VERSION, "1.0" );
    }

    private final Map<String, StringBuilder> services = new HashMap<String, StringBuilder>();
    private final PrintLogger logger;

    public UberJar( PrintLogger logger )
    {
        this.logger = logger;
    }

    public static void main( String[] args ) throws IOException
    {
        if ( args.length != 2 )
        {
            throw new IllegalArgumentException( "Expects exactly two arguments: target jar and classpath" );
        }
        UberJar uberJar = new UberJar(new DateStampingPrintLogger( System.out ));
        String mainClass = System.getProperty( "Main-Class", null );
        if ( mainClass != null )
        {
            uberJar.setMainClass( mainClass );
        }
        uberJar.createJar( args[0], args[1].split( ":" ) );
    }

    private void setMainClass( String className )
    {
        manifest.getMainAttributes().put( Attributes.Name.MAIN_CLASS, className );
    }

    public void createJar( String uberJarFileName, String... sourceLocations ) throws IOException
    {
        logger.print( "Creating über jar '%s'", uberJarFileName );
        JarOutputStream target = new JarOutputStream( new FileOutputStream( uberJarFileName ), manifest );
        try
        {
            for ( String sourceLocation : sourceLocations )
            {
                File sourceFile = new File( sourceLocation );
                if ( !sourceFile.exists() )
                {
                    throw new IllegalArgumentException(
                            String.format( "Source location '%s' does not exist.", sourceLocation ) );
                }
                else if ( sourceFile.isDirectory() )
                {
                    create( "", sourceFile, target );
                }
                else
                {
                    copy( new JarFile( sourceFile ), target );
                }
            }
            long timestamp = System.currentTimeMillis();
            for ( Map.Entry<String, StringBuilder> service : services.entrySet() )
            {
                JarEntry entry = new JarEntry( service.getKey() );
                entry.setTime( timestamp );
                if ( !add( target, entry ) )
                {
                    throw new IllegalStateException(
                            String.format( "Service '%s' should not have been added at this stage.",
                                           service.getKey() ) );
                }
                target.write( service.getValue().toString().getBytes() );
                target.closeEntry();
            }
        }
        finally
        {
            target.close();
        }
        logger.print( "Created über jar '%s'", uberJarFileName );
    }

    private void addService( String path, InputStream source ) throws IOException
    {
        StringBuilder target = services.get( path );
        if ( target == null )
        {
            services.put( path, target = new StringBuilder() );
        }
        BufferedReader reader = new BufferedReader( new InputStreamReader( source ) );
        try
        {
            for ( String line; null != (line = reader.readLine()); )
            {
                target.append( line ).append( '\n' );
            }
        }
        finally
        {
            reader.close();
        }
    }

    private void create( String path, File source, JarOutputStream target ) throws IOException
    {
        if ( source.isDirectory() )
        {
            if ( !path.isEmpty() )
            {
                path += '/';
                JarEntry entry = new JarEntry( path );
                entry.setTime( source.lastModified() );
                if ( add( target, entry ) )
                {
                    target.closeEntry();
                }
            }
            for ( String child : source.list() )
            {
                create( path + child, new File( source, child ), target );
            }
        }
        else if ( path.startsWith( SERVICES_PREFIX ) )
        {
            addService( path, new FileInputStream( source ) );
        }
        else
        {
            JarEntry entry = new JarEntry( path );
            entry.setTime( source.lastModified() );
            if ( !add( target, entry ) )
            {
                return;
            }
            FileInputStream input = new FileInputStream( source );
            try
            {
                copy( input, target );
            }
            finally
            {
                input.close();
            }
            target.closeEntry();
        }
    }

    private void copy( JarFile source, JarOutputStream target ) throws IOException
    {
        for ( Enumeration<JarEntry> entries = source.entries(); entries.hasMoreElements(); )
        {
            JarEntry entry = entries.nextElement();
            if ( "META-INF/MANIFEST.MF".equals( entry.getName() ) )
            {
                continue; // Skip the manifest file
            }
            if ( entry.getName().startsWith( "META-INF/" ) && anySuffix( entry.getName(), ".SF", ".RSA", ".DSA" ) )
            {
                continue; // Skip signature files
            }
            if ( !entry.isDirectory() && entry.getName().startsWith( SERVICES_PREFIX ) )
            {
                addService( entry.getName(), source.getInputStream( entry ) );
            }
            else if ( add( target, entry ) )
            {
                if ( !entry.isDirectory() )
                {
                    copy( source.getInputStream( entry ), target );
                }
                target.closeEntry();
            }
        }
    }

    private boolean anySuffix( String string, String... suffixes )
    {
        for ( String suffix : suffixes )
        {
            if ( string.endsWith( suffix ) )
            {
                return true;
            }
        }
        return false;
    }

    private void copy( InputStream source, OutputStream target ) throws IOException
    {
        BufferedInputStream in = new BufferedInputStream( source );
        try
        {
            for ( int count; -1 != (count = in.read( buffer )); )
            {
                target.write( buffer, 0, count );
            }
        }
        finally
        {
            in.close();
        }
    }

    private boolean add( JarOutputStream target, JarEntry entry ) throws IOException
    {
        try
        {
            target.putNextEntry( entry );
            return true;
        }
        catch ( ZipException e )
        {
            return false;
        }
    }
}
