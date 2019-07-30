package com.lyyang.activitistudy;

import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author yyli16
 * @date 2019/7/29
 * @time 15:05
 *
 * 此代码纯面向过程，无设计模式，无可扩展性，只用于activiti基础功能演示
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class QJLCTest {

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

        //启动流程
        startProcess();

        //用户任务管理
        userTaskManagementV2();
    }

    /**
     * 创建组
     */
    private void createGroup(){
        // 创建并保存组对象
        Group group = identityService.newGroup("deptLeader");
        group.setName("部门领导");
        group.setType("assignment");
        identityService.saveGroup(group);
        // 验证组是否已保存成功，首先需要创建组查询对象
        List<Group> groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        assertEquals(1, groupList.size());
    }

    /**
     *  创建用户
     */
    private void createUser(){
        // 创建并保存用户对象
        User user1 = identityService.newUser("小明");
        identityService.saveUser(user1);

        User user2 = identityService.newUser("班长");
        identityService.saveUser(user2);

        User user3 = identityService.newUser("班主任");
        identityService.saveUser(user3);

        User user4 = identityService.newUser("教导主任");
        identityService.saveUser(user4);

    }

    private void queryGroup(){
        // 查询属于组deptLeader的用户
        User userInGroup = identityService.createUserQuery().memberOfGroup("deptLeader").singleResult();
        assertNotNull(userInGroup);
        assertEquals("henryyan", userInGroup.getId());
    }

    private void queryUser(){
        // 查询henryyan所属组
        Group groupContainsHenryyan = identityService.createGroupQuery().groupMember("henryyan").singleResult();
        assertNotNull(groupContainsHenryyan);
        assertEquals("deptLeader", groupContainsHenryyan.getId());
    }

    /**
     * 删除组
     */
    private void deleteGroup(){
        // 删除组
        identityService.deleteGroup("deptLeader");
        // 验证是否删除成功
        List<Group> groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        assertEquals(0, groupList.size());
    }

    /**
     * 删除用户
     */
    private void deleteUser(){
        // 删除用户
        identityService.deleteUser("henryyan");
        // 验证是否删除成功
        User userInDb = identityService.createUserQuery().userId("henryyan").singleResult();
        assertNull(userInDb);
    }

    /**
     * 将用户加入组
     */
    private void addUserToGroup(){
        // 把用户henryyan加入到组deptLeader中
        identityService.createMembership("henryyan", "deptLeader");
    }

    /**
     * 将用户从组中移除
     */
    private void deleteUserFromGroup(){
        // 把用户henryyan加入到组deptLeader中
        identityService.deleteMembership("henryyan", "deptLeader");
    }

    /**
     * 流程部署
     */
    private void myDeploy(){
        //部署bpmn文件
        String bpmnClasspath = "bpmn/flow2.bpmn";
        // 创建部署构建器
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        // 添加资源
        deploymentBuilder.addClasspathResource(bpmnClasspath);
        // 执行部署
        deploymentBuilder.deploy();

        // 验证流程定义是否部署成功
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> myProcess = processDefinitionQuery.processDefinitionKey("qjlc").list();
        System.out.println("process count: " + myProcess.size());
//        assertEquals(1, count);
    }

    /**
     * 启动流程
     */
    private void startProcess(){
        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qjlc");
//        ProcessInstance processInstance = runtimeService.startProcessInstanceById("myProcess_2:3:2505");
        assertNotNull(processInstance);
    }

    /**
     * 用户任务管理
     */
    private void userTaskManagement(){
        //班长
        String userId = "班长";
        Task task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        //签收任务
        taskService.claim(task.getId(), userId);
        //处理任务
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("bz_status","同意");
        taskService.complete(task.getId(),taskVariables);

        //班主任
        userId = "班主任";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        taskVariables.put("bzr_status","同意");
        taskService.complete(task.getId(),taskVariables);

        //教导主任
        userId = "教导主任";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        taskVariables.put("jdzr_status","同意");
        taskService.complete(task.getId(),taskVariables);

        //小明销假
        userId = "小明";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskService.complete(task.getId());
    }


    /**
     * 用户任务管理
     */
    private void userTaskManagementV2(){
        //班长
        String userId = "班长";
        Task task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        //签收任务
        taskService.claim(task.getId(), userId);
        //处理任务
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("bz_status","同意且紧急");
        taskService.complete(task.getId(),taskVariables);

        //教导主任
        userId = "教导主任";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        taskVariables.put("jdzr_status","拒绝");
        taskService.complete(task.getId(),taskVariables);

        //小明取消申请
        userId = "小明";
        task = taskService.createTaskQuery().taskCandidateUser(userId).singleResult();
        taskService.claim(task.getId(), userId);
        taskVariables = new HashMap<>();
        taskVariables.put("xm_status","撤销申请");
        taskService.complete(task.getId(),taskVariables);
    }
}
