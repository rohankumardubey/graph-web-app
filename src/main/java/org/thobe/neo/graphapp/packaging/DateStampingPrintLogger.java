package org.thobe.neo.graphapp.packaging;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class DateStampingPrintLogger implements PrintLogger
{
    private final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z: " );

    {
        dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    private final PrintStream out;

    public DateStampingPrintLogger( PrintStream out )
    {
        this.out = out;
    }

    @Override
    public void print( String format, Object... parameters )
    {
        out.println( dateFormat.format( new Date() ) + String.format( format, parameters ) );
    }
}
