package com.ecommerce.eurekaserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EurekaServerApplicationTest {

    @Test
    @DisplayName("Test 1: Main class instantiation")
    void testMainClassInstantiation() {
        EurekaServerApplication application = new EurekaServerApplication();
        assertNotNull(application);
    }

    @Test
    @DisplayName("Test 2: Verify @SpringBootApplication annotation is present")
    void testSpringBootApplicationAnnotation() {
        SpringBootApplication annotation = EurekaServerApplication.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation);
    }

    @Test
    @DisplayName("Test 3: Verify @EnableEurekaServer annotation is present")
    void testEnableEurekaServerAnnotation() {
        EnableEurekaServer annotation = EurekaServerApplication.class.getAnnotation(EnableEurekaServer.class);
        assertNotNull(annotation);
    }

    @Test
    @DisplayName("Test 4: Verify application package naming")
    void testPackageNaming() {
        assertEqualsPackage("com.ecommerce.eurekaserver", EurekaServerApplication.class.getPackageName());
    }

    @Test
    @DisplayName("Test 5: Verify class name")
    void testClassName() {
        assertTrue(EurekaServerApplication.class.getSimpleName().contains("EurekaServerApplication"));
    }

    @Test
    @DisplayName("Test 6: Verify default constructor is public")
    void testPublicConstructor() throws NoSuchMethodException {
        assertNotNull(EurekaServerApplication.class.getConstructor());
    }

    private void assertEqualsPackage(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
