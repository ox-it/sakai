package org.springframework.web.servlet.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag a controller as only applying to a particular FilteredAnnotationHandlerMapping
 * 
 * @author Matthew Buckett
 * @see FilteredAnnotationHandlerMapping
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TargettedController {
    /**
     * The values to check against. No pattern matching is done.
     */
    String[] value() default {""};

}
