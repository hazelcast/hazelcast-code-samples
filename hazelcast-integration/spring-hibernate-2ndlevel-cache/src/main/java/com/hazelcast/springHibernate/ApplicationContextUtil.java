package com.hazelcast.springHibernate;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class ApplicationContextUtil implements ApplicationContextAware {
 
  private static ApplicationContext applicationContext;
 
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }
  
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	  ApplicationContextUtil.applicationContext = applicationContext; 
  } 
  
}