package fr.github.vera.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.github.vera.documention.ResponseApi;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces({MediaType.APPLICATION_JSON, "application/vnd.sun.wadl+xml"})
public class ResponseApiMessageBodyWriter implements MessageBodyWriter<ResponseApi<?>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ResponseApi.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(ResponseApi<?> responseApi, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {
        objectMapper.writeValue(entityStream, responseApi);
    }
}