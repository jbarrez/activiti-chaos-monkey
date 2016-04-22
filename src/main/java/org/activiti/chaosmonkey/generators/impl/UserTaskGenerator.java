package org.activiti.chaosmonkey.generators.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.chaosmonkey.generators.AbstractGenerator;

public class UserTaskGenerator extends AbstractGenerator {

  protected int taskCount = 0;

  public boolean isApplicable(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    return processHasStartEvent(bpmnModel);
  }

  public FlowElement generate(BpmnModel bpmnModel, FlowElement currentFlowElement) {
    UserTask userTask = new UserTask();
    userTask.setId("userTask" + taskCount);
    userTask.setName(userTask.getId());
    taskCount = taskCount + 1;
    
    addFlowelement(bpmnModel, currentFlowElement, userTask);
    addSequenceFlow(currentFlowElement.getParentContainer(), currentFlowElement, userTask);
    
    return userTask;
  }
  
  @Override
  public void reset() {
    super.reset();
    this.taskCount = 0;
  }
  
}
