function cypher( query, parameters, callback )
{
    var xhr = new XMLHttpRequest();
    xhr.open( "POST", "/neo4j/cypher", true );
    xhr.setRequestHeader( "Content-type", "application/json" );
    xhr.onreadystatechange = function ()
    {
        if ( this.readyState == this.DONE )
        {
            if ( this.status == 200 )
            {
                callback( JSON.parse( this.responseText ) );
            }
            else
            {
                callback();
            }
        }
    };
    xhr.send( JSON.stringify( {query: query, parameters: parameters} ) );
}

cypher.createIfAbsent = function ( index, key, value, properties, callback )
{
    var xhr = new XMLHttpRequest();
    xhr.open( "PUT", "/neo4j/cypher", ((typeof callback) != "undefined") );
    xhr.setRequestHeader( "Content-type", "application/json" );
    if ( callback )
    {
        xhr.onreadystatechange = function ()
        {
            if ( this.readyState == this.DONE )
            {
                callback();
            }
        };
    }
    var payload = {index: index, key: key, value: value};
    if ( ((typeof properties) != "undefined") )
    {
        payload.properties = properties;
    }
    xhr.send( JSON.stringify( payload ) );
};