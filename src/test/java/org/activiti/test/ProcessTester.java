package org.activiti.test;


import java.util.LinkedList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTester {
  
  private static final Logger logger = LoggerFactory.getLogger(ProcessTester.class);
  
  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  
  protected BpmnModel bpmnModel;
  protected org.activiti.bpmn.model.Process process;
  
  protected String processInstanceId;
  
  protected int nrOfStartEvents;
  protected int nrOfEndEvents;
  protected int nrOfSubProcesses;
  protected int nrOfUserTasks;
  
  protected int nrOfAssertEquals;
  protected int nrOfAssertNulls;
  protected int nrOfAssertNotNulls;
  protected int nrOfAssertTrues;
  
  
  public ProcessTester(ProcessEngine processEngine, BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
    this.processEngine = processEngine;
    this.repositoryService = processEngine.getRepositoryService();
    this.runtimeService = processEngine.getRuntimeService();
    this.taskService = processEngine.getTaskService();
    this.historyService = processEngine.getHistoryService();
    this.identityService = processEngine.getIdentityService();
    this.managementService = processEngine.getManagementService();
  }
  
  public void executeTests() {
    long start = System.currentTimeMillis();
    process = bpmnModel.getProcesses().get(0);
    
    // Using while loop to avoid too deep nested stack calls
    LinkedList<FlowNode> flowNodesToHandle = new LinkedList<FlowNode>();
    visitStartEvent(process, flowNodesToHandle);
    while (!flowNodesToHandle.isEmpty()) {
      handleFlowNode(flowNodesToHandle.poll(), flowNodesToHandle);
    }
   
    long end = System.currentTimeMillis();
    long time = (end-start);
    
    shouldBeEqual("No executions should exist when the test has ended", 0, runtimeService.createExecutionQuery().count());
    
    logger.info("Summary: visited {} start events, {} user tasks, {} subprocesses and {} end events in {} ms. Executed {} assertEquals, {} assertNulls, {} assertNotNulls and {} assertTrues.", 
        nrOfStartEvents, nrOfUserTasks, nrOfSubProcesses, nrOfEndEvents, time, nrOfAssertEquals, nrOfAssertNulls, nrOfAssertNotNulls, nrOfAssertTrues);
  }

  protected void visitStartEvent(FlowElementsContainer flowElementsContainer, LinkedList<FlowNode> flowNodesToHandle)  {
    StartEvent startEvent = findStartEvent(flowElementsContainer);
    
    if (flowElementsContainer instanceof org.activiti.bpmn.model.Process) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess");
      shouldNotBeNull(processInstance);
      this.processInstanceId = processInstance.getId();

      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId()).singleResult();
      shouldNotBeNull(historicProcessInstance.getStartTime());
    }

    flowNodesToHandle.add(startEvent);
  }
  
  protected StartEvent findStartEvent(FlowElementsContainer flowElementsContainer) {
    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
      if (flowElement instanceof StartEvent) {
        return (StartEvent) flowElement;
      }
    }
    return null;
  }
  
  protected void followSequenceFlow(FlowNode flowNode, LinkedList<FlowNode> flowNodesToHandle) {
    List<SequenceFlow> sequenceFlows = flowNode.getOutgoingFlows();
    if (sequenceFlows != null && sequenceFlows.size() > 0) {
      for (SequenceFlow sequenceFlow : sequenceFlows) {
        FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
        if (targetFlowElement instanceof FlowNode) {
          FlowNode targetFlowNode = (FlowNode) targetFlowElement;
          flowNodesToHandle.add(targetFlowNode);
        }
      }
    } else if (flowNode.getSubProcess() != null) {
      followSequenceFlow(flowNode.getSubProcess(), flowNodesToHandle);
    }
  }
  
  protected void handleFlowNode(FlowNode flowNode, LinkedList<FlowNode> flowNodesToHandle)  {
    if (flowNode instanceof SubProcess) {
      handleSubProcess((SubProcess) flowNode, flowNodesToHandle);
      nrOfSubProcesses++;
    } else if (flowNode instanceof StartEvent) { 
      followSequenceFlow(flowNode, flowNodesToHandle);
      nrOfStartEvents++;
    } else if (flowNode instanceof UserTask) {
      handleUserTask((UserTask) flowNode);
      followSequenceFlow(flowNode, flowNodesToHandle);
      nrOfUserTasks++;
    } else if (flowNode instanceof EndEvent) {
      handleEndEvent((EndEvent) flowNode);
      followSequenceFlow(flowNode, flowNodesToHandle);
      nrOfEndEvents++;
    }
  }
  
  protected void handleSubProcess(SubProcess subProcess, LinkedList<FlowNode> flowNodesToHandle)  {
    
    // Verify execution tree structure:
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).activityId(subProcess.getId()).list();
    shouldBeTrue(executions != null && executions.size() > 0);
    
    for (Execution executionInSubProcess : executions) {
      SubProcess parentSubProcess = subProcess.getSubProcess();
      while (parentSubProcess != null) {
        Execution parentExecution = runtimeService.createExecutionQuery().executionId(executionInSubProcess.getParentId()).singleResult();
        shouldNotBeNull("Expected an execution for the parent subprocess", parentExecution);
        shouldBeEqual(parentSubProcess.getId(), parentExecution.getActivityId());
        parentSubProcess = parentSubProcess.getSubProcess();
      }
    }
    
    visitStartEvent(subProcess, flowNodesToHandle);
  }
  
  protected void handleUserTask(UserTask userTask)  {
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(userTask.getId()).singleResult();
    shouldNotBeNull(task);
    shouldBeEqual(userTask.getName(), task.getName());
    taskService.complete(task.getId());
    
    HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
    shouldBeEqual(task.getName(), historicTask.getName());
    shouldNotBeNull("Historic task should have start time", historicTask.getStartTime());
    shouldNotBeNull("Historic task should have end time", historicTask.getEndTime());
    
  }
  
  protected void handleEndEvent(EndEvent endEvent)  {
    if (endEvent.getParentContainer() instanceof Process) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstanceId).singleResult();
      shouldNotBeNull("Historic process instance should have start time", historicProcessInstance.getStartTime());
      shouldNotBeNull("Historic process instance should have end time", historicProcessInstance.getEndTime());
    }
  }
  
  // Helper methods
  
  protected void shouldBeEqual(Object left, Object rigjt) {
    Assert.assertEquals(left, rigjt);
    nrOfAssertEquals++;
  }
  
  protected void shouldBeEqual(String message, Object left, Object rigjt) {
    Assert.assertEquals(message, left, rigjt);
    nrOfAssertEquals++;
  }
  
  protected void shouldBeEqual(long left, long rigjt) {
    Assert.assertEquals(left, rigjt);
    nrOfAssertEquals++;
  }
  
  protected void shouldBeEqual(String message, long left, long rigjt) {
    Assert.assertEquals(message, left, rigjt);
    nrOfAssertEquals++;
  }
  
  protected void shouldBeNull(String message, Object obj) {
    Assert.assertNull(message, obj);
    nrOfAssertNulls++;
  }
  
  protected void shouldBeNull(Object obj) {
    Assert.assertNull(obj);
    nrOfAssertNulls++;
  }
  
  protected void shouldNotBeNull(String message, Object obj) {
    Assert.assertNotNull(message, obj);
    nrOfAssertNotNulls++;
  }
  
  protected void shouldNotBeNull(Object obj) {
    Assert.assertNotNull(obj);
    nrOfAssertNotNulls++;
  }
  
  protected void shouldBeTrue(boolean condition) {
    Assert.assertTrue(condition);
    nrOfAssertTrues++;
  }
  
}
