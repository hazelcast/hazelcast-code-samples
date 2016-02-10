package com.hazelcast.spring.springaware;

import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class SomeTask implements Callable<String>, ApplicationContextAware, Serializable {

    private transient ApplicationContext context;

    @Autowired
    private transient IDummyBean dummyBean;

    public String call() throws Exception {
        return dummyBean.getCity();
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }

    public void setDummyBean(DummyBean dummyBean) {
        this.dummyBean = dummyBean;
    }
}
