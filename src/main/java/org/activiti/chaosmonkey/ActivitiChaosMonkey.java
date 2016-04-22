package org.activiti.chaosmonkey;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.activiti.chaosmonkey.generators.impl.ProcessDefinitionGenerator;
import org.activiti.chaosmonkey.testgeneration.TestProjectGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivitiChaosMonkey {
  
  private static final Logger logger = LoggerFactory.getLogger(ActivitiChaosMonkey.class);
  
  public static final String PROPERTY_OUTPUT_FOLDER = "output.folder";
  public static final String PROPERTY_PROCESS_GENERATION_TIME = "process.generation.time";
  public static final String PROPERTY_MAX_PROCESS_ELEMENTS = "max.process.elements";
  
  protected Properties properties;
  protected String outputFolderName;
  protected long generationTime;
  protected long maxNrProcessElements;
  
  public ActivitiChaosMonkey(Properties properties) {
    this.properties = properties;
    initProperties();
  }
  
  protected void initProperties() {
    this.outputFolderName = properties.getProperty(PROPERTY_OUTPUT_FOLDER);
    this.generationTime = Long.valueOf(properties.getProperty(PROPERTY_PROCESS_GENERATION_TIME));
    this.maxNrProcessElements = Long.valueOf(properties.getProperty(PROPERTY_MAX_PROCESS_ELEMENTS));
  }
  
  public void goWild() {
    
    System.out.println("Using output folder '" + outputFolderName + "'");
    System.out.println("Randomly generating process definition for " + generationTime + "ms");
    System.out.println("Process definitions will be limited to " + maxNrProcessElements + " elements");
    
    if (new File(outputFolderName).exists()) {
      throw new RuntimeException("Folder " + outputFolderName + " already exists");
    }
    
    final ProcessDefinitionGenerator processDefinitionGenerator = new ProcessDefinitionGenerator(outputFolderName, maxNrProcessElements);
    final Thread generationThread = createAndStartGenerationThread(outputFolderName, processDefinitionGenerator);
    scheduleGenerationInterrupt(generationTime, processDefinitionGenerator);
    
    try {
      generationThread.join();
    } catch (InterruptedException e) {
      logger.warn("Could not join the process definition generation thread", e);
    }
    
    logger.info("Done generating process definitions.");
    logger.info("Starting to generate test project");
    
    // Generate a maven test project from the output folder
    generateMavenTestProject(outputFolderName);
  }

  protected Thread createAndStartGenerationThread(String outputFolderName, final ProcessDefinitionGenerator processDefinitionGenerator) {
    final Thread t = new Thread(new Runnable() {
      public void run() {
        processDefinitionGenerator.generateProcessDefinitions();
      }
    });
    t.start();
    return t;
  }
  
  protected void scheduleGenerationInterrupt(long nrOfSeconds, final ProcessDefinitionGenerator processDefinitionGenerator) {
    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    exec.schedule(new Runnable() {
      public void run() {
        processDefinitionGenerator.stop();
      }
    }, nrOfSeconds, TimeUnit.SECONDS);
  }
  
  protected void generateMavenTestProject(String outputFolderName) {
    new TestProjectGenerator(outputFolderName).generate();;
  }

}
