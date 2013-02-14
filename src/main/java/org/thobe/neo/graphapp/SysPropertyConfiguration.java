package org.thobe.neo.graphapp;

import java.io.File;

public class SysPropertyConfiguration implements Configuration
{
    private final int port;
    private final File storeDir;

    SysPropertyConfiguration()
    {
        this.port = Integer.parseInt( System.getProperty( "neo4j.app.port", "8080" ) );
        this.storeDir = new File( System.getProperty( "neo4j.app.storeDir", new File( "graphdb" ).getAbsolutePath() ) );
        if ( storeDir.isFile() )
        {
            throw new IllegalArgumentException( storeDir + " is not a directory!" );
        }
    }

    public int port()
    {
        return port;
    }

    public String storeDir()
    {
        return storeDir.getAbsolutePath();
    }
}
