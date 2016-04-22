package org.activiti;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.activiti.chaosmonkey.ActivitiChaosMonkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
  
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  public static void main(String[] args) throws Exception {
    
    Properties properties = loadProperties();
    ActivitiChaosMonkey chaosMonkey = new ActivitiChaosMonkey(properties);
    chaosMonkey.goWild();
    
    logger.info("All done.");
    System.exit(0);
  }

  protected static Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    File file = new File("./chaos-monkey.properties");
    InputStream inputStream = null;
    if (file.exists()) {
      logger.info("Using chaos-monkey.properties settings");
      inputStream = new FileInputStream(file);
    } else {
      logger.info("No chaos-monkey.properties found, using default.properties");
      inputStream = Main.class.getClassLoader().getResourceAsStream("default.properties");
    }
    properties.load(inputStream);
    inputStream.close();
    return properties;
  }

}
