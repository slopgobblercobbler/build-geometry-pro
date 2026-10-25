package com.slopgobbler.buildgeometry.testrunner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * High-speed standalone test runner capable of executing JUnit 5 test suites
 * directly from native build scripts or CI pipelines.
 */
public class StandaloneTestRunner {

    public static void main(String[] args) {
        Class<?>[] testClasses = {
                com.slopgobbler.buildgeometry.math.Point3iTest.class,
                com.slopgobbler.buildgeometry.math.Vec3dTest.class,
                com.slopgobbler.buildgeometry.math.BoundingBox3iTest.class,
                com.slopgobbler.buildgeometry.math.DiscreteCurvesTest.class,
                com.slopgobbler.buildgeometry.generator.CircleGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.EllipseGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.CatenaryArchGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.ParabolicDomeGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.SphereGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.SpiralGeneratorTest.class,
                com.slopgobbler.buildgeometry.generator.PolyhedronGeneratorTest.class,
                com.slopgobbler.buildgeometry.render.LayerSlicerTest.class,
                com.slopgobbler.buildgeometry.guide.AngleSnapManagerTest.class
        };

        System.out.println("================================================================================");
        System.out.println(" BUILDGEOMETRY PRO - JUNIT 5 TEST EXECUTION SUITE");
        System.out.println("================================================================================");

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        List<String> failures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (Class<?> testClass : testClasses) {
            System.out.println("\nRunning " + testClass.getSimpleName() + "...");
            Object instance;
            try {
                instance = testClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("  Failed to instantiate " + testClass.getName() + ": " + e.getMessage());
                failedTests++;
                continue;
            }

            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    totalTests++;
                    String testName = method.getName();
                    if (method.isAnnotationPresent(DisplayName.class)) {
                        testName = method.getAnnotation(DisplayName.class).value();
                    }

                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                        passedTests++;
                        System.out.println("  [PASS] " + testName);
                    } catch (Throwable t) {
                        failedTests++;
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        String failInfo = testClass.getSimpleName() + "." + method.getName() + ": " + cause.getMessage();
                        failures.add(failInfo);
                        System.err.println("  [FAIL] " + testName);
                        cause.printStackTrace(System.err);
                    }
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(String.format("TEST EXECUTION COMPLETED in %d ms", elapsed));
        System.out.println(String.format("Total: %d | Passed: %d | Failed: %d", totalTests, passedTests, failedTests));
        System.out.println("--------------------------------------------------------------------------------");

        if (failedTests > 0) {
            System.err.println("\nFAILURES (" + failedTests + "):");
            for (String f : failures) {
                System.err.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100% PASS RATE)");
            System.exit(0);
        }
    }
}
