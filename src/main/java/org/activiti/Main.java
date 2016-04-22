package org.activiti;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.activiti.chaosmonkey.ActivitiChaosMonkey;


public class Main {
  
  public static void main(String[] args) throws Exception {
    
    Properties properties = loadProperties();
    ActivitiChaosMonkey chaosMonkey = new ActivitiChaosMonkey(properties);
    chaosMonkey.goWild();
    
    System.out.println("All done.");
    System.exit(0);
  }

  protected static Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("chaos-monkey.properties");
    if (inputStream == null) {
      inputStream = Main.class.getClassLoader().getResourceAsStream("default.properties");
    }
    properties.load(inputStream);
    inputStream.close();
    return properties;
  }

}
