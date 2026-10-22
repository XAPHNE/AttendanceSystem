package com.bsmi.attendancesystem.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Marks a test class that clicks through the real screens. These tests need a display, so they
 * are skipped on a Linux machine without one (run them there with {@code xvfb-run ./mvnw verify}),
 * and {@code -DexcludedGroups=ui} skips them anywhere.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// Lets screen tests get the skip rule and tag from the base class they extend
@Inherited
@Tag("ui")
@EnabledIf(
        value = "com.bsmi.attendancesystem.ui.Displays#available",
        disabledReason = "no display; run with xvfb-run")
public @interface UiTest {}
