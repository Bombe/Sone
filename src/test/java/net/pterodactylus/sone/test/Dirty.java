package net.pterodactylus.sone.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation marks tests or test methods that are dirty,
 * i.e. written in a way no test should ever be written in.
 */
@Retention(SOURCE)
@Target(value = { TYPE, METHOD })
public @interface Dirty {

	String value() default "";

}
