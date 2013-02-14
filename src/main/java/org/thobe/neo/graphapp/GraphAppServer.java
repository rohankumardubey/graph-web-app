package org.thobe.neo.graphapp;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class GraphAppServer
{
    public static void main( String... args ) throws Exception
    {
        final GraphAppServer server = createWithEmbeddedGraphDatabase( new SysPropertyConfiguration() );
        Runtime.getRuntime().addShutdownHook( new Thread( "Shut down GraphAppServer" )
        {
            @Override
            public void run()
            {
                try
                {
                    System.out.println( "Stopping server..." );
                    server.stop();
                }
                catch ( Exception e )
                {
                    System.err.println( "Failure when stopping server:" );
                    e.printStackTrace( System.err );
                }
            }
        } );
        server.run( args );
    }

    public static GraphAppServer createWithEmbeddedGraphDatabase( Configuration configuration )
    {
        System.out.println( "Starting embedded graph database on dir: " + configuration.storeDir() );
        return new GraphAppServer( new EmbeddedGraphDatabase( configuration.storeDir() ),
                                   configuration );
    }

    private final GraphDatabaseService graphdb;
    private final Configuration configuration;

    GraphAppServer( GraphDatabaseService graphdb, Configuration configuration )
    {
        this.graphdb = graphdb;
        this.configuration = configuration;
    }

    private Server server;

    void run( String... args ) throws Exception
    {
        if ( args == null || args.length != 1 )
        {
            throw new IllegalArgumentException( "Expected exactly one argument: <static source root>" );
        }
        start( args[0] );
    }

    public synchronized void start( String staticSources ) throws Exception
    {
        if ( server != null )
        {
            throw new IllegalStateException( "The server has already been started" );
        }

        server = new Server( configuration.port() );

        WebAppContext context = new WebAppContext( server, staticSources, "/" );

        context.addServlet( new ServletHolder( new CypherServlet( new JsonCypher( new ExecutionEngine( graphdb ) ) ) ),
                            "/neo4j/cypher" );

        context.addServlet( staticFile( "application/javascript", "/cypher.js" ), "/neo4j/cypher.js" );

        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            stop();
            throw e;
        }
    }

    private ServletHolder staticFile( String contentType, String resourceName ) throws IOException
    {
        URL resource = getClass().getResource( resourceName );
        if ( resource == null )
        {
            throw new IllegalArgumentException( "No such resource: " + resourceName );
        }
        return new ServletHolder( new FileServlet( contentType, resource ) );
    }

    public synchronized void stop() throws Exception
    {
        try
        {
            if ( server != null )
            {
                try
                {
                    server.stop();
                }
                finally
                {
                    graphdb.shutdown();
                }
            }
        }
        finally
        {
            server = null;
        }
    }
}
