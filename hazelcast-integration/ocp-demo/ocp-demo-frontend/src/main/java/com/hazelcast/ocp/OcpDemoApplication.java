package com.hazelcast.ocp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@SpringBootApplication
@ComponentScan(basePackages = "com.hazelcast.ocp")
public class OcpDemoApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(OcpDemoApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/assets/");

    }
}
