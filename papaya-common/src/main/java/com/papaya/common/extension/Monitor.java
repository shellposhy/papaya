package com.papaya.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation tag for {@code Rpc} service {@code Method}
 * <p>
 * This annotation can be used to mark special cases, such as service errors,
 * service exceptions, or when a service is offline.
 * 
 * @author shellpo shih
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Monitor {
	// Describe method functionality.
	String value() default "";

	// Whether to provide external functions
	EMonitor type() default EMonitor.Online;
}
