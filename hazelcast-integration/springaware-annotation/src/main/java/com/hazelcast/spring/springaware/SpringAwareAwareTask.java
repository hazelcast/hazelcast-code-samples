package com.hazelcast.spring.springaware;

import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class SpringAwareAwareTask implements Callable<String>, ApplicationContextAware, Serializable {

    private transient ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
      throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String call() throws Exception {
        System.out.println("Getting bean definition names from Spring Context");
        return String.join(",", applicationContext.getBeanDefinitionNames());
    }
}
