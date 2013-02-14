package org.thobe.neo.graphapp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.PropertyContainer;

import static java.util.Collections.emptyMap;

public class JsonCypher
{
    private final ExecutionEngine executionEngine;

    public JsonCypher( ExecutionEngine executionEngine )
    {
        this.executionEngine = executionEngine;
    }

    public JsonNode execute( String query, JsonNode parameters )
    {
        ExecutionResult result = executionEngine.execute( query, mapOf( parameters ) );
        ArrayNode json = JsonNodeFactory.instance.arrayNode();
        for ( Map<String, Object> row : result )
        {
            transform( row, json.addObject() );
        }
        return json;
    }

    static void transform( Map<String, Object> map, ObjectNode json )
    {
        for ( Map.Entry<String, Object> entry : map.entrySet() )
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            put( json, key, value );
        }
    }

    private static void put( ObjectNode json, String key, Object value )
    {
        if ( value instanceof Map<?, ?> )
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) value;
            transform( mapValue, json.with( key ) );
        }
        else if ( value instanceof PropertyContainer )
        {
            PropertyContainer properties = (PropertyContainer) value;
            ObjectNode child = json.with( key );
            for ( String propertyKey : properties.getPropertyKeys() )
            {
                put( child, propertyKey, properties.getProperty( propertyKey ) );
            }
        }
        else if ( value instanceof Collection<?> )
        {
            ArrayNode list = json.putArray( key );
            for ( Object element : (Collection<?>) value )
            {
                add( list, element );
            }
        }
        else if ( value instanceof Object[] )
        {
            ArrayNode list = json.putArray( key );
            for ( Object element : (Object[]) value )
            {
                add( list, element );
            }
        }
        else if ( value == null )
        {
            json.putNull( key );
        }
        else if ( value instanceof String )
        {
            json.put( key, (String) value );
        }
        else if ( value instanceof Boolean )
        {
            json.put( key, (Boolean) value );
        }
        else if ( value instanceof Long )
        {
            json.put( key, (Long) value );
        }
        else if ( value instanceof Integer )
        {
            json.put( key, (Integer) value );
        }
        else if ( value instanceof Double )
        {
            json.put( key, (Double) value );
        }
        else if ( value instanceof Float )
        {
            json.put( key, (Float) value );
        }
        else if ( value instanceof Short )
        {
            json.put( key, (Short) value );
        }
        else if ( value instanceof Byte )
        {
            json.put( key, (Byte) value );
        }
        else if ( value instanceof Character )
        {
            json.put( key, value.toString() );
        }
        else if ( value instanceof char[] )
        {
            json.put( key, new String( (char[]) value ) );
        }
        else if ( value.getClass().isArray() )
        {
            ArrayNode list = json.putArray( key );
            for ( int i = 0, size = Array.getLength( value ); i < size; i++ )
            {
                add( list, Array.get( value, i ) );
            }
        }
        else
        {
            throw new IllegalArgumentException( "Unsupported type: " + value.getClass() );
        }
    }

    private static void add( ArrayNode list, Object value )
    {
        if ( value instanceof Map<?, ?> )
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            transform( map, list.addObject() );
        }
        else if ( value instanceof Collection<?> )
        {
            ArrayNode nested = list.addArray();
            for ( Object element : (Collection<?>) value )
            {
                add( nested, element );
            }
        }
        else if ( value instanceof Object[] )
        {
            ArrayNode nested = list.addArray();
            for ( Object element : (Object[]) value )
            {
                add( nested, element );
            }
        }
        else if ( value == null )
        {
            list.addNull();
        }
        else if ( value instanceof String )
        {
            list.add( (String) value );
        }
        else if ( value instanceof Boolean )
        {
            list.add( (Boolean) value );
        }
        else if ( value instanceof Long )
        {
            list.add( (Long) value );
        }
        else if ( value instanceof Integer )
        {
            list.add( (Integer) value );
        }
        else if ( value instanceof Double )
        {
            list.add( (Double) value );
        }
        else if ( value instanceof Float )
        {
            list.add( (Float) value );
        }
        else if ( value instanceof Short )
        {
            list.add( (Short) value );
        }
        else if ( value instanceof Byte )
        {
            list.add( (Byte) value );
        }
        else if ( value instanceof Character )
        {
            list.add( value.toString() );
        }
        else if ( value instanceof char[] )
        {
            list.add( new String( (char[]) value ) );
        }
        else if ( value.getClass().isArray() )
        {
            ArrayNode nested = list.addArray();
            for ( int i = 0, size = Array.getLength( value ); i < size; i++ )
            {
                add( nested, Array.get( value, i ) );
            }
        }
        else
        {
            throw new IllegalArgumentException( "Unsupported type: " + value.getClass() );
        }
    }

    static Object transform( JsonNode json )
    {
        if ( json.isNull() )
        {
            return null;
        }
        if ( json.isObject() )
        {
            return mapOf( json );
        }
        if ( json.isArray() )
        {
            List<Object> list = new ArrayList<Object>();
            for ( JsonNode entry : json )
            {
                list.add( transform( entry ) );
            }
            return list;
        }
        if ( json.isTextual() )
        {
            return json.getTextValue();
        }
        if ( json.isNumber() )
        {
            switch ( json.getNumberType() )
            {
            case INT:
                return json.getIntValue();
            case LONG:
                return json.getLongValue();
            case BIG_INTEGER:
                return json.getBigIntegerValue();
            case FLOAT:
                return json.getDoubleValue(); // TODO: is there no float method?
            case DOUBLE:
                return json.getDoubleValue();
            case BIG_DECIMAL:
                return json.getDecimalValue();
            }
            return json.getNumberValue();
        }
        if ( json.isBoolean() )
        {
            return json.getBooleanValue();
        }
        throw new IllegalArgumentException( "Cannot convert " + json );
    }

    private static Map<String, Object> mapOf( JsonNode json )
    {
        if ( json == null || json.isNull() )
        {
            return emptyMap();
        }
        if ( !json.isObject() )
        {
            throw new IllegalArgumentException( "Parameters has to be an object!" );
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for ( Iterator<String> fields = json.getFieldNames(); fields.hasNext(); )
        {
            String field = fields.next();
            map.put( field, transform( json.get( field ) ) );
        }
        return map;
    }
}
