/*
 * File: DenyUser.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package utilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows for given controller methods to deny access to logged in users.
 *
 * Based off of the secure module:
 * https://github.com/playframework/play/tree/master/modules/secure
 *
 * @author Peter Graham
 * @author Guillaume Bort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DenyUser {
    /** an array of controller method names to allow any visitors to access */
    String[] value();
}