package org.thobe.neo.graphapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileServlet extends HttpServlet
{
    private final String body;
    private final String contentType;

    public FileServlet( String contentType, URL resource ) throws IOException
    {
        this.contentType = contentType;
        StringBuilder body = new StringBuilder();
        InputStream inputStream = resource.openStream();
        byte[] buffer = new byte[4096];
        for ( int read; (read = inputStream.read( buffer )) > 0; )
        {
            body.append( new String( buffer, 0, read ) );
        }
        this.body = body.toString();
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
    {
        resp.setContentType( contentType );
        PrintWriter writer = new PrintWriter( resp.getOutputStream() );
        writer.write( body );
        writer.flush();
    }
}
