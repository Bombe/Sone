package net.pterodactylus.sone.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation marks test methods that are somehow not good test methods.
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Dirty {

	String value() default "";

}
