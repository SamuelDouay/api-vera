package fr.github.vera.filters;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@SecurityRequirement(name = "BearerAuth")
public @interface Secured {
    String[] roles() default {};
}