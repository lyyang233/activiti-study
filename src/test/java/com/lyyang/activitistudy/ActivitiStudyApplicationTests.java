package com.lyyang.activitistudy;

import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiStudyApplicationTests {

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

//    @Autowired
//    private UserRepository userRepository;

    @Test
    public void contextLoads() {

        // 创建并保存组对象
        Group group = identityService.newGroup("deptLeader");
        group.setName("部门领导");
        group.setType("assignment");
        identityService.saveGroup(group);
        // 验证组是否已保存成功，首先需要创建组查询对象
        List<Group> groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        assertEquals(1, groupList.size());

        // 创建并保存用户对象
        User user = identityService.newUser("henryyan");
        user.setFirstName("Henry");
        user.setLastName("Yan");
        user.setEmail("yanhonglei@gmail.com");
        identityService.saveUser(user);
        // 验证用户是否保存成功
        User userInDb = identityService.createUserQuery().userId("henryyan").singleResult();
        assertNotNull(userInDb);


        // 把用户henryyan加入到组deptLeader中
        identityService.createMembership("henryyan", "deptLeader");


        // 删除用户
        identityService.deleteUser("henryyan");
        // 验证是否删除成功
        userInDb = identityService.createUserQuery().userId("henryyan").singleResult();
        assertNull(userInDb);

        // 删除组
        identityService.deleteGroup("deptLeader");
        // 验证是否删除成功
        groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        assertEquals(0, groupList.size());
    }

}
