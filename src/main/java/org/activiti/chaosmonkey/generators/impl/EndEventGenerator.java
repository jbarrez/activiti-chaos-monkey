package org.activiti.chaosmonkey.generators.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.chaosmonkey.generators.AbstractGenerator;

public class EndEventGenerator extends AbstractGenerator {
  
  protected int endEventCount = 0;

  public boolean isApplicable(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    return processHasStartEvent(bpmnModel);
  }

  public FlowElement generate(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    EndEvent endEvent = new EndEvent();
    endEvent.setId("endEvent" + endEventCount);
    endEventCount = endEventCount + 1;
    
    addFlowelement(bpmnModel, currentFlowElement, endEvent);
    addSequenceFlow(currentFlowElement.getParentContainer(), currentFlowElement, endEvent);
    
    // In case the end event is added to a subprocess, return that element so the generation can continue from that point on 
    if (endEvent.getSubProcess() != null) {
      return endEvent.getSubProcess();
    }
    
    return null;
  }
  
  @Override
  public void reset() {
    super.reset();
    this.endEventCount = 0;
  }
  
}
