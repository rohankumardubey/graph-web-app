package org.thobe.neo.graphapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class CypherServlet extends HttpServlet
{
    private final JsonCypher cypher;
    private ObjectMapper mapper = new ObjectMapper();

    public CypherServlet( JsonCypher cypher )
    {
        this.cypher = cypher;
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
    {
        JsonNode payload = mapper.readTree( req.getInputStream() );
        JsonNode result = cypher.execute( payload.get( "query" ).getTextValue(), payload.get( "parameters" ) );
        mapper.writeTree( new JsonFactory().createJsonGenerator( resp.getOutputStream() ), result );
    }
}
