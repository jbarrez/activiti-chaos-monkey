package org.activiti.chaosmonkey.testgeneration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a Maven project with unit tests.
 * 
 * Assumes that the folder path passed in the constructor has a process definitions in it
 * for which unit tests must be generated. 
 */
public class TestProjectGenerator {
  
  private static final Logger logger = LoggerFactory.getLogger(TestProjectGenerator.class);
  
  protected File outputFolder;
  
  protected File srcFolder;
  protected File mainFolder;
  protected File javaFolder;
  protected File resourcesFolder;
  protected File testFolder;
  protected File testJavaFolder;
  protected File testResourcesFolder;
  protected File unitTestFolder;
  
  public TestProjectGenerator(String outputFolderPath) {
    outputFolder = new File(outputFolderPath);
    if (outputFolder.isFile()) {
      throw new RuntimeException(outputFolderPath + " should be a folder!");
    }
  }
  
  public void generate() {
    createMavenFolders();
    moveBpmnFiles();
    copyPomXml();
    copyLogConfig();
    copyActivitiCfg();
    copyHelperClasses();
    generateUnitTests();
  }

  protected void createMavenFolders() {
    
    logger.info("Generating Maven project structure");
    
    srcFolder = new File(outputFolder, "src");
    srcFolder.mkdir();
    
    mainFolder = new File(srcFolder, "main");
    mainFolder.mkdir();
    
    javaFolder = new File(mainFolder, "java");
    javaFolder.mkdir();
    
    resourcesFolder = new File(mainFolder, "resources");
    resourcesFolder.mkdir();
    
    testFolder = new File(srcFolder, "test");
    testFolder.mkdir();
    
    testJavaFolder = new File(testFolder, "java");
    testJavaFolder.mkdir();
    
    testResourcesFolder = new File(testFolder, "resources");
    testResourcesFolder.mkdir();
    
    unitTestFolder = new File(testJavaFolder, "org/activiti/test");
    unitTestFolder.mkdirs();
  }
  
  protected void moveBpmnFiles() {
    
    logger.info("Moving bpmn20.xml files");
    
    Path sourceDir = outputFolder.toPath();
    Path destinationDir = testResourcesFolder.toPath();

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourceDir)) {
        for (Path path : directoryStream) {
            if (path.getFileName().toString().endsWith("bpmn20.xml")) {
              System.out.println("copying " + path.toString());
              Path d2 = destinationDir.resolve(path.getFileName());
              System.out.println("destination File=" + d2);
              Files.move(path, d2, StandardCopyOption.ATOMIC_MOVE);
            }
        }
    } catch (IOException e) {
      logger.warn("Could not move bpmn20.xml files", e);
    }
  }
  
  protected void copyPomXml() {
    copyResource("pom-template.xml", "pom.xml", outputFolder);
  }
  
  protected void copyLogConfig() {
    copyResource("log4j.properties", "log4j.properties", testResourcesFolder);
  }
  
  protected void copyActivitiCfg() {
    copyResource("activiti-template.cfg.xml", "activiti.cfg.xml", testResourcesFolder);
  }
  
  protected void copyHelperClasses() {
    copyResource("process-tester-template.txt", "ProcessTester.java", unitTestFolder);
  }
  
  protected void copyResource(String resource, String newName, File folder) {
    logger.info("Copying {} to ", resource, folder.getAbsolutePath());
    try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource)) {
      FileUtils.copyInputStreamToFile(inputStream, new File(folder, newName));
    } catch (IOException e) {
      logger.warn("Could not copy " + resource, e);
    }
  }
  
  protected void generateUnitTests() {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(testResourcesFolder.toPath())) {
      for (Path path : directoryStream) {
        if (path.getFileName().toString().endsWith("bpmn20.xml")) {
          UnitTestGenerator unitTestGenerator = new UnitTestGenerator(path.toFile(), unitTestFolder);
          unitTestGenerator.generate();
        }
      }
    } catch (IOException e) {
      logger.warn("Could not read bpmn20.xml files", e);
    }
  }
  
}
