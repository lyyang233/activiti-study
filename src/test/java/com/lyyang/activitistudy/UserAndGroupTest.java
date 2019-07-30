package com.lyyang.activitistudy;

/**
 * @author yyli16
 * @date 2019/7/30
 * @time 9:36
 */

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

import java.util.List;

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
public class UserAndGroupTest {

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

        //创建组
        createGroup();

        //将用户加入组
        addUserToGroup();

        //流程部署
        myDeploy();

        //启动流程
        startProcess();

        //用户任务管理
        userTaskManagement();
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
        User user = identityService.newUser("billie");
        user.setFirstName("billie");
        user.setLastName("eilish");
        user.setEmail("billie@gmail.com");
        identityService.saveUser(user);
        // 验证用户是否保存成功
        User userInDb = identityService.createUserQuery().userId("billie").singleResult();
        assertNotNull(userInDb);

        User user1 = identityService.newUser("jackchen");
        user1.setFirstName("Jack");
        user1.setLastName("Chen");
        user1.setEmail("jackchen@gmail.com");
        identityService.saveUser(user1);
        User userInDb1 = identityService.createUserQuery().userId("jackchen").singleResult();
        assertNotNull(userInDb1);
    }

    private void queryGroup(){
        // 查询属于组deptLeader的用户
        User userInGroup = identityService.createUserQuery().memberOfGroup("deptLeader").singleResult();
        assertNotNull(userInGroup);
        assertEquals("billie", userInGroup.getId());
    }

    private void queryUser(){
        // billie
        Group groupContainsHenryyan = identityService.createGroupQuery().groupMember("billie").singleResult();
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
        identityService.deleteUser("billie");
        // 验证是否删除成功
        User userInDb = identityService.createUserQuery().userId("billie").singleResult();
        assertNull(userInDb);
    }

    /**
     * 将用户加入组
     */
    private void addUserToGroup(){
        // 把用户billie加入到组deptLeader中
        identityService.createMembership("billie", "deptLeader");
        // 把用户jackchen加入到组deptLeader中
        identityService.createMembership("jackchen", "deptLeader");
    }

    /**
     * 将用户从组中移除
     */
    private void deleteUserFromGroup(){
        // 把用户billie加入到组deptLeader中
        identityService.deleteMembership("billie", "deptLeader");
    }

    /**
     * 流程部署
     */
    private void myDeploy(){
        //部署bpmn文件
        String bpmnClasspath = "bpmn/flow.bpmn";
        // 创建部署构建器
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        // 添加资源
        deploymentBuilder.addClasspathResource(bpmnClasspath);
        // 执行部署
        deploymentBuilder.deploy();

        // 验证流程定义是否部署成功
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> myProcess = processDefinitionQuery.processDefinitionKey("myProcess_3").list();
        System.out.println("process count: " + myProcess.size());
//        assertEquals(1, count);
    }

    /**
     * 启动流程
     */
    private void startProcess(){
        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess_3");
//        ProcessInstance processInstance = runtimeService.startProcessInstanceById("myProcess_2:3:2505");
        assertNotNull(processInstance);
    }

    /**
     * 用户任务管理
     */
    private void userTaskManagement(){
        //billie作为候选人的任务
        Task billieTask = taskService.createTaskQuery().taskCandidateUser("billie").singleResult();
        Task jackchenTask = taskService.createTaskQuery().taskCandidateUser("jackchen").singleResult();
//        System.out.println("test");
        assertNotNull(billieTask);
        //签收任务
        taskService.claim(billieTask.getId(), "billie");
        //billie签收任务后，jackchen无法签收任务
        jackchenTask = taskService.createTaskQuery().taskCandidateUser("jackchen").singleResult();

        //处理任务
        taskService.complete(billieTask.getId());
    }
}

