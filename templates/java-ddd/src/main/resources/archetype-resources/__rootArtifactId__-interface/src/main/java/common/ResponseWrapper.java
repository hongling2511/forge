#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Automatically wraps controller responses in ApiResponse.
 * Excludes responses that are already ApiResponse or error responses.
 */
@RestControllerAdvice(basePackages = "${package}")
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip if already wrapped or is an error response
        Class<?> declaringClass = returnType.getDeclaringClass();
        String className = declaringClass.getSimpleName();

        // Skip GlobalExceptionHandler responses (they handle their own wrapping)
        if (className.contains("ExceptionHandler")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        // Already wrapped
        if (body instanceof ApiResponse) {
            return body;
        }

        // Wrap in ApiResponse
        return ApiResponse.success(body);
    }
}
