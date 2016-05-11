package uk.ac.ox.oucs.oxam.readers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks the field should be included as well by looking inside
 * the target class for @ColumnMapping and @Include annotations.
 * @author buckett
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Include {

}
