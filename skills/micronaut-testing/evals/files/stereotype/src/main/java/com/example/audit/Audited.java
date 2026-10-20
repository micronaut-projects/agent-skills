package com.example.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a bean whose method calls are written to the audit log.
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Audited {
}
