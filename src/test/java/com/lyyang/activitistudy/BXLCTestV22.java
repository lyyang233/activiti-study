package com.lyyang.activitistudy;

import org.activiti.engine.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

/**
 * @author yyli16
 * @date 2019/7/30
 * @time 15:33
 *
 * 此代码纯面向过程，无设计模式，无可扩展性，只用于activiti基础功能演示
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class BXLCTestV22 {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private FormService formService;

    @Test
    public void contextLoads() {

        //创建用户
        createUser();

        //流程部署
        myDeploy();

        //启动流程并用户任务管理
        userTaskManagement();
    }

    /**
     * 创建用户
     */
    private void createUser() {
        // 创建并保存用户对象
        User user1 = identityService.newUser("审批人1");
        identityService.saveUser(user1);

        User user2 = identityService.newUser("审批人2");
        identityService.saveUser(user2);

        User user3 = identityService.newUser("审批人3");
        identityService.saveUser(user3);

        User user4 = identityService.newUser("申请人");
        identityService.saveUser(user4);
    }

    /**
     * 流程部署
     */
    private void myDeploy() {
        //部署bpmn文件
        String bpmnClasspath = "bpmn/flow3-2.bpmn";
        // 创建部署构建器
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        // 添加资源
        deploymentBuilder.addClasspathResource(bpmnClasspath);
        // 执行部署
        deploymentBuilder.deploy();

        // 验证流程定义是否部署成功
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> myProcess = processDefinitionQuery.processDefinitionKey("bxlcv22").list();
        System.out.println("process count: " + myProcess.size());
//        assertEquals(1, count);
    }

    /**
     * 用户任务管理
     */
    private void userTaskManagement() {

        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bxlcv22");
        assertNotNull(processInstance);

        Map<String, Object> taskVariables;
        //查询当前所有tasks
        List<Task> currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //申请人
        String userId = "申请人";
        Task task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        //签收任务
        taskService.claim(task.getId(), userId);
        //处理任务
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //审批人1
        userId = "审批人1";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        int spr1_status = 1;
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //审批人2
        userId = "审批人2";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        int spr2_status = 0;
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //临时节点
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskVariables = new HashMap<>();
        taskVariables.put("spr_status",spr1_status & spr2_status);
        taskService.complete(task.getId(),taskVariables);

        //回到申请人
        userId = "申请人";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //审批人1
        userId = "审批人1";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        spr1_status = 1;
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //审批人2
        userId = "审批人2";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        spr2_status = 1;
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //临时节点
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskVariables = new HashMap<>();
        taskVariables.put("spr_status",spr1_status & spr2_status);
        taskService.complete(task.getId(),taskVariables);

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        //审批人3
        userId = "审批人3";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskService.complete(task.getId());

        //查询当前所有tasks
        currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    }
}
