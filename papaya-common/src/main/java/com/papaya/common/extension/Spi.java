package com.papaya.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for extension interface
 * <p/>
 * Changes on extension configuration file <br/>
 * Use <code>Protocol</code> as an example, its configuration file
 * 'META-INF/services/com.xxx.Protocol' is changes from: <br/>
 * 
 * <pre>
 *     com.foo.XxxProtocol
 *     com.foo.YyyProtocol
 * </pre>
 * <p>
 * to key-value pair <br/>
 * 
 * <pre>
 *     xxx=com.foo.XxxProtocol
 *     yyy=com.foo.YyyProtocol
 * </pre>
 * 
 * <br/>
 * The reason for this change is:
 * <p>
 * If there's third party library referenced by static field or by method in
 * extension implementation, its class will fail to initialize if the third
 * party library doesn't exist.
 * <p/>
 * For example:
 * <p>
 * Fails to load Extension("mina"). When user configure to use mina, papaya will
 * complain the extension cannot be loaded, instead of reporting which extract
 * extension implementation fails and the extract reason.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Spi {
	/**
	 * default extension name
	 */
	String value() default "";
}
