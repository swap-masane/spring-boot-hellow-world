package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import springfox.documentation.spring.web.plugins.Docket;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SwaggerConfigTest {

    @Autowired
    private Docket docket;

    @Test
    public void testSwaggerConfig() {
        assertNotNull(docket);
    }
}
