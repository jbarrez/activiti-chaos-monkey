package org.activiti.chaosmonkey.generators.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.chaosmonkey.generators.AbstractGenerator;

public class StartEventGenerator extends AbstractGenerator {

  protected int startEventCount = 0;
  
  public boolean isApplicable(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    if (hasProcess(bpmnModel)) {
      org.activiti.bpmn.model.Process process = getProcess(bpmnModel);
       List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class);
       if (startEvents.size() == 0) {
         return true;
       }
    }
    return false;
  }

  public FlowElement generate(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    StartEvent startEvent = new StartEvent();
    startEvent.setId("startEvent" + startEventCount);
    startEventCount = startEventCount + 1;
    
    addFlowelement(bpmnModel, currentFlowElement, startEvent);
    
    return startEvent;
  }
  
  
  @Override
  public void reset() {
    super.reset();
    this.startEventCount = 0;
  }

}
