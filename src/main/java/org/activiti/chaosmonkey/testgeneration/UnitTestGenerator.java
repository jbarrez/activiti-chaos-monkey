package org.activiti.chaosmonkey.testgeneration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates one java unit test (as a .java file) for a given process definition.
 * 
 * Responsible for creating the file and the boilerplate code.
 * Calls the {@link UnitTestLogicGenerator} to generate the actual logic
 * and adds it in the boilerplate template.
 */
public class UnitTestGenerator {
  
  private static final Logger logger = LoggerFactory.getLogger(UnitTestGenerator.class);
  
  protected File bpmnXmlFile;
  protected File outputFolder;
  
  public UnitTestGenerator(File bpmnXmlFile, File outputFolder) {
    this.bpmnXmlFile = bpmnXmlFile;
    this.outputFolder = outputFolder;
  }
  
  public void generate() {
    logger.info("Generating unit test for " + bpmnXmlFile);

    try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test-template.txt")) {
      String testTemplate = IOUtils.toString(inputStream);
      testTemplate = testTemplate.replace("$bpmn20Xml$", bpmnXmlFile.getName());
      
      String processName = bpmnXmlFile.getName().replace(".bpmn20.xml", "");
      String processNameUpperCased = processName.substring(0, 1).toUpperCase() + processName.substring(1);
      String finalFileName = "Test" + processNameUpperCased;
      testTemplate = testTemplate.replace("MyTest", finalFileName);
      File file = new File(outputFolder, finalFileName + ".java");
      FileUtils.write(file, testTemplate);
      
    } catch (IOException e) {
      logger.warn("Exception while reading test template", e);
    }
  }
  
}
