package org.activiti.chaosmonkey.generators.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.chaosmonkey.generators.Generator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDefinitionGenerator {
  
  private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionGenerator.class);
  
  protected String outputFolder;
  protected long maxNrOfProcessElements;
  
  protected boolean stopRequested;
  
  protected List<Generator> generators = new ArrayList<Generator>();
  protected Random random = new Random();
  
  public ProcessDefinitionGenerator(String outputFolder, long maxNrOfProcessElements) {
    
    this.outputFolder = outputFolder;
    this.maxNrOfProcessElements = maxNrOfProcessElements;
    
    generators.add(new StartEventGenerator());
    generators.add(new EndEventGenerator());
    generators.add(new UserTaskGenerator());
    generators.add(new EmbeddedSubProcessGenerator());
  }
  
  
  public void stop() {
    stopRequested = true;
  }
  
  public void generateProcessDefinitions() {
    Set<String> generatedProcessDefinitionHashes = new HashSet<String>();
    while (!stopRequested) {
      String processDefinitionXml = generateRandomBpmnXml();
      String md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(processDefinitionXml);
      if (!generatedProcessDefinitionHashes.contains(md5Hash)) {
        File file = new File(outputFolder + "/process" + (generatedProcessDefinitionHashes.size() + 1) + ".bpmn20.xml");
        generatedProcessDefinitionHashes.add(md5Hash);
        try {
          FileUtils.write(file, processDefinitionXml);
          logger.info("Generated " + generatedProcessDefinitionHashes.size() + " process definitions");
        } catch (IOException e) {
          logger.warn("Could not write process definition xml", e);
        }
      }
    }
  }
  
  public String generateRandomBpmnXml() {
    BpmnModel bpmnModel = generateRandomBpmnModel();
    BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    byte[] bytes = bpmnXMLConverter.convertToXML(bpmnModel, "UTF-8");
    return new String(bytes);
  }
  
  public BpmnModel generateRandomBpmnModel() {
    
    BpmnModel bpmnModel = createBpmnModel();
    for (Generator generator : generators) {
      generator.reset();
    }
    
    FlowElement currentFlowElement = null;
    int nrOfElementsAdded = 0;
    while (!stopRequested 
        && (!hasFlowElements(bpmnModel) || currentFlowElement != null)
        && nrOfElementsAdded < maxNrOfProcessElements) { // continue if no elements were added yet or the last returned wasn't null (meaning process end was reached)
      
      List<Generator> possibleGenerators = new ArrayList<Generator>();
      for (Generator generator : generators) {
        if (generator.isApplicable(bpmnModel, currentFlowElement)) {
          possibleGenerators.add(generator);
        }
      }
      
      if (possibleGenerators.size() == 0) {
        throw new RuntimeException("Programmatic error: no possible generators could be found for bpmnModel " + bpmnModel);
      }
      
      Generator generator = possibleGenerators.get(random.nextInt(possibleGenerators.size()));
      currentFlowElement = generator.generate(bpmnModel, currentFlowElement);
      nrOfElementsAdded++;
      
    }
    
    return bpmnModel;
  }
  
  protected boolean hasFlowElements(BpmnModel bpmnModel) {
    return bpmnModel.getProcesses() != null 
        && bpmnModel.getProcesses().size() > 0
        && bpmnModel.getProcesses().get(0).getFlowElements().size() > 0;
  }
  
  protected BpmnModel createBpmnModel() {
    BpmnModel bpmnModel = new BpmnModel();
    org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
    process.setId("myProcess");
    bpmnModel.addProcess(process);
    return bpmnModel;
  }

}
