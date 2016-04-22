package org.activiti.chaosmonkey.generators.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.chaosmonkey.generators.AbstractGenerator;

public class EmbeddedSubProcessGenerator extends AbstractGenerator {

  protected int subProcessCount = 0;
  
  @Override
  public boolean isApplicable(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    return processHasStartEvent(bpmnModel);
  }

  @Override
  public FlowElement generate(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    SubProcess subProcess = new SubProcess();
    subProcess.setId("subProcess" + subProcessCount);
    subProcess.setName(subProcess.getId());
    
    addFlowelement(bpmnModel, currentFlowElement, subProcess);
    addSequenceFlow(currentFlowElement.getParentContainer(), currentFlowElement, subProcess);
    
    StartEvent startEvent = new StartEvent();
    startEvent.setId("startEventSubProcess" + subProcessCount);
    subProcess.addFlowElement(startEvent);
    
    subProcessCount = subProcessCount + 1;
    
    return startEvent;
    
  }
  
  @Override
  public void reset() {
    super.reset();
    subProcessCount = 0;
  }

}
