package com.dr.alfresco.repo.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ApplicationContextHolder.applicationContext = applicationContext;
  }
  
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

}
