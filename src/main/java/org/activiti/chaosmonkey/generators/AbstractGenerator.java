package org.activiti.chaosmonkey.generators;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;

public abstract class AbstractGenerator implements Generator {
  
  public static int sequenceFlowCount = 0; // sequenceflow couns are shared between generators
  
  protected boolean hasProcess(BpmnModel bpmnModel) {
    return getProcess(bpmnModel) != null;
  }
  
  protected Process getProcess(BpmnModel bpmnModel) {
    if (bpmnModel.getProcesses() == null || bpmnModel.getProcesses().size() == 0) {
      return null;
    }
    return bpmnModel.getProcesses().get(0);
  }
  
  protected boolean processHasStartEvent(BpmnModel bpmnModel) {
    if (hasProcess(bpmnModel)) {
      List<StartEvent> startEvents = getProcess(bpmnModel).findFlowElementsOfType(StartEvent.class);
      return startEvents != null && startEvents.size() > 0;
    }
    return false;
  }
  
  protected void addSequenceFlow(FlowElementsContainer flowElementsContainer, FlowElement sourceFlowElement, FlowElement targetFlowElement) {
    SequenceFlow sequenceFlow = new SequenceFlow();
    sequenceFlow.setId("sequenceFlow" + sequenceFlowCount);
    sequenceFlowCount = sequenceFlowCount + 1;
    sequenceFlow.setSourceFlowElement(sourceFlowElement);
    sequenceFlow.setSourceRef(sourceFlowElement.getId());
    sequenceFlow.setTargetFlowElement(targetFlowElement);
    sequenceFlow.setTargetRef(targetFlowElement.getId());
    flowElementsContainer.addFlowElement(sequenceFlow);
  }
  
  protected void addFlowelement(BpmnModel bpmnModel, FlowElement currentFlowElement, FlowElement flowElementToAdd) {
    if (currentFlowElement != null && currentFlowElement.getParentContainer() != null) {
      currentFlowElement.getParentContainer().addFlowElement(flowElementToAdd);
    } else {
      getProcess(bpmnModel).addFlowElement(flowElementToAdd);
    }
  }
  
  @Override
  public void reset() {
    sequenceFlowCount = 0;
  }

}
