package org.thobe.neo.graphapp;

import java.io.File;

public class ExecuteSampleServer implements Configuration
{
    public static void main( String... args ) throws Exception
    {
        File cwd = new File( "." ).getAbsoluteFile();
        File target = new File( cwd, "target" );
        if ( !target.exists() )
        {
            throw new IllegalStateException( "cannot find target dir" );
        }
        File resources = new File( cwd, "src/test/resources" );
        if ( !resources.exists() )
        {
            throw new IllegalStateException( "cannot find resources dir" );
        }
        GraphAppServer.createWithEmbeddedGraphDatabase( new ExecuteSampleServer( target ) )
                      .start( resources.getAbsolutePath() );
    }

    private final String storeDir;

    private ExecuteSampleServer( File target )
    {
        this.storeDir = new File( target, "test-data/" + getClass().getName() + "/graphdb" ).getAbsolutePath();
    }

    @Override
    public int port()
    {
        return 8080;
    }

    @Override
    public String storeDir()
    {
        return storeDir;
    }
}
