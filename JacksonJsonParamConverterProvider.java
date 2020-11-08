package io.swagger.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class JacksonJsonParamConverterProvider implements ParamConverterProvider {

    @Context
    private Providers providers;

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, Type genericType, final Annotation[] annotations) {
        final MessageBodyReader<T> mbr = providers.getMessageBodyReader(
                rawType, genericType, annotations, MediaType.APPLICATION_JSON_TYPE);


        if(mbr == null ||
                !mbr.isReadable(rawType, genericType, annotations, MediaType.APPLICATION_JSON_TYPE))
            return null;

        final ObjectMapper objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new JodaModule())
                .setDateFormat(new RFC3339DateFormat());

        return new ParamConverter<T>() {
            @Override
            public T fromString(String value) {
                if(value == null)
                    return null;
                
                try {
                    return objectMapper.readerFor(rawType).readValue(value);
                } catch (Exception e)   {
                    e.printStackTrace();
                    throw new ProcessingException(e);
                }
            }

            @Override
            public String toString(T t) {
                try{
                    return objectMapper.writerFor(rawType).writeValueAsString(t);
                } catch (Exception e)   {
                    e.printStackTrace();
                    throw new ProcessingException(e);
                }
            }
        };
    }
}
