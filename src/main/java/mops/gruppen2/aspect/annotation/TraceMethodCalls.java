package mops.gruppen2.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schreibt eine Nachricht für jede ausgeführte Methode einer Klasse in den Trace-Stream
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceMethodCalls {
}
