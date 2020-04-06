package mops.gruppen2.service;

import mops.gruppen2.Gruppen2Application;
import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO: Alles in die entsprechenden Klassen sortieren :((((
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Gruppen2Application.class)
@Transactional
@Rollback
class ControllerServiceTest {

    Account account;
    Account account2;
    Account account3;
    @Autowired
    ControllerService controllerService;
    @Autowired
    EventStoreService eventStoreService;
    @Autowired
    ValidationService validationService;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    GroupService groupService;
    @Autowired
    InviteService inviteService;
    @Autowired
    SearchService searchService;
    @Autowired
    ProjectionService projectionService;

    @BeforeEach
    void setUp() {
        Set<String> roles = new HashSet<>();
        roles.add("l");
        account = new Account("ich", "ich@hhu.de", "l", "ichdude", "jap", roles);
        account2 = new Account("ich2", "ich2@hhu.de", "l", "ichdude2", "jap2", roles);
        account3 = new Account("ich3", "ich3@hhu.de", "l", "ichdude3", "jap3", roles);
        eventRepository.deleteAll();
    }

    @Test
    void createPublicGroupWithNoParentAndLimitedNumberTest() {
        controllerService.createGroup(account, "test", "hi", null, null, null, 20L, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPublicGroupWithNoParentAndUnlimitedNumberTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPrivateGroupWithNoParentAndUnlimitedNumberTest() {
        controllerService.createGroup(account, "test", "hi", true, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPrivateGroupWithNoParentAndLimitedNumberTest() {
        controllerService.createGroup(account, "test", "hi", true, null, null, 20L, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPrivateGroupWithParentAndLimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account2, "test", "hi", null, true, true, null, null, null);
        List<Group> groups1 = projectionService.projectUserGroups(account2.getName());
        controllerService.createGroup(account, "test", "hi", true, null, null, 20L, groups1.get(0).getId());
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertEquals(groups1.get(0).getId(), groups.get(0).getParent());
    }

    @Test
    void createPublicGroupWithParentAndLimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account2, "test", "hi", null, null, true, null, null, null);
        List<Group> groups1 = projectionService.projectUserGroups(account2.getName());
        controllerService.createGroup(account, "test", "hi", null, null, null, 20L, groups1.get(0).getId());
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertEquals(groups1.get(0).getId(), groups.get(0).getParent());
    }

    @Test
    void createPublicGroupWithParentAndUnlimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account2, "test", "hi", null, null, true, null, null, null);
        List<Group> groups1 = projectionService.projectUserGroups(account2.getName());
        controllerService.createGroup(account, "test", "hi", null, true, true, null, groups1.get(0).getId());
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertEquals(groups1.get(0).getId(), groups.get(0).getParent());
    }

    @Test
    void createPrivateGroupWithParentAndUnlimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account2, "test", "hi", null, null, true, null, null, null);
        List<Group> groups1 = projectionService.projectUserGroups(account2.getName());
        controllerService.createGroup(account, "test", "hi", true, true, true, null, groups1.get(0).getId());
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertEquals(groups1.get(0).getId(), groups.get(0).getParent());
    }

    @Test
    void createPublicOrgaGroupWithNoParentAndLimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", null, null, null, 20L, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.SIMPLE, groups.get(0).getType());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPublicOrgaGroupWithNoParentAndUnlimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", null, null, true, null, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.SIMPLE, groups.get(0).getType());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPrivateOrgaGroupWithNoParentAndLimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", true, null, null, 20L, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.SIMPLE, groups.get(0).getType());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createPrivateOrgaGroupWithNoParentAndUnlimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", true, null, true, null, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.SIMPLE, groups.get(0).getType());
        assertEquals(Visibility.PRIVATE, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createOrgaLectureGroupAndLimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", null, true, null, 20L, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.LECTURE, groups.get(0).getType());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(20L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    @Test
    void createOrgaLectureGroupAndUnlimitedNumberTest() throws IOException {
        controllerService.createGroupAsOrga(account, "test", "hi", null, true, true, null, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        testTitleAndDescription(groups.get(0).getTitle(), groups.get(0).getDescription());
        assertEquals(GroupType.LECTURE, groups.get(0).getType());
        assertEquals(Visibility.PUBLIC, groups.get(0).getVisibility());
        assertEquals(100000L, groups.get(0).getUserMaximum());
        assertNull(groups.get(0).getParent());
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    public void deleteUserTest() {
        controllerService.createGroup(account, "test", "hi", true, true, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        User user = new User(account.getName(), "", "", "");
        groupService.deleteUser(account, user, groups.get(0));
        assertTrue(projectionService.projectUserGroups(account.getName()).isEmpty());
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    public void updateRoleAdminTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        User user = new User(account.getName(), "", "", "");
        groupService.updateRole(user, groups.get(0).getId());
        groups = projectionService.projectUserGroups(account.getName());
        assertEquals(Role.MEMBER, groups.get(0).getRoles().get(account.getName()));
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    public void updateRoleMemberTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        User user = new User(account2.getName(), "", "", "");
        groupService.updateRole(user, groups.get(0).getId());
        groups = projectionService.projectUserGroups(account.getName());
        assertEquals(Role.ADMIN, groups.get(0).getRoles().get(account2.getName()));
    }

    //TODO: GroupServiceTest
    @Test
    public void updateRoleNonUserTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        User user = new User(account2.getName(), "", "", "");
        Throwable exception = assertThrows(UserNotFoundException.class, () -> groupService.updateRole(user, groups.get(0).getId()));
        assertEquals("404 NOT_FOUND \"Der User wurde nicht gefunden.    (class mops.gruppen2.service.ValidationService)\"", exception.getMessage());
    }

    //TODO: GroupServiceTest
    @Test
    public void deleteNonUserTest() {
        controllerService.createGroup(account, "test", "hi", true, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        User user = new User(account2.getName(), "", "", "");
        Throwable exception = assertThrows(UserNotFoundException.class, () -> groupService.deleteUser(account, user, groups.get(0)));
        assertEquals("404 NOT_FOUND \"Der User wurde nicht gefunden.    (class mops.gruppen2.service.ValidationService)\"", exception.getMessage());
    }

    void testTitleAndDescription(String title, String description) {
        assertEquals("test", title);
        assertEquals("hi", description);
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    void passIfLastAdminTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        User user = new User(account.getName(), "", "", "");
        groups = projectionService.projectUserGroups(account2.getName());
        groupService.deleteUser(account, user, groups.get(0));
        groups = projectionService.projectUserGroups(account2.getName());
        assertEquals(Role.ADMIN, groups.get(0).getRoles().get(account2.getName()));
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    void dontPassIfNotLastAdminTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        User user2 = new User(account2.getName(), "", "", "");
        groupService.updateRole(user2, groups.get(0).getId());
        groupService.addUser(account3, groups.get(0).getId());
        groupService.changeRoleIfLastAdmin(account, groups.get(0));
        User user = new User(account.getName(), "", "", "");
        groupService.deleteUser(account, user, groups.get(0));
        groups = projectionService.projectUserGroups(account2.getName());
        assertEquals(Role.MEMBER, groups.get(0).getRoles().get(account3.getName()));
    }

    //TODO: GroupServiceTest
    @Disabled
    @Test
    void getVeteranMemberTest() {
        controllerService.createGroup(account, "test", "hi", null, null, true, null, null);
        List<Group> groups = projectionService.projectUserGroups(account.getName());
        groupService.addUser(account2, groups.get(0).getId());
        groupService.addUser(account3, groups.get(0).getId());
        User user = new User(account.getName(), "", "", "");
        groups = projectionService.projectUserGroups(account2.getName());
        groupService.deleteUser(account, user, groups.get(0));
        groups = projectionService.projectUserGroups(account2.getName());
        assertEquals(Role.ADMIN, groups.get(0).getRoles().get(account2.getName()));
        assertEquals(Role.MEMBER, groups.get(0).getRoles().get(account3.getName()));
    }
}
