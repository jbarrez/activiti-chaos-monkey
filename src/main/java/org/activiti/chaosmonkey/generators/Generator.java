package org.activiti.chaosmonkey.generators;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;

public interface Generator {
  
  boolean isApplicable(BpmnModel bpmnModel, FlowElement currentFlowElement);
  
  FlowElement generate(BpmnModel bpmnModel, FlowElement currentFlowElement);
  
  void reset();
  
}
