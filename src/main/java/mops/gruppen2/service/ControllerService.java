package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.event.CreateGroupEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Log4j2
public class ControllerService {

    private final EventStoreService eventStoreService;
    private final InviteService inviteService;
    private final GroupService groupService;

    public ControllerService(EventStoreService eventStoreService, InviteService inviteService, GroupService groupService) {
        this.eventStoreService = eventStoreService;
        this.inviteService = inviteService;
        this.groupService = groupService;
    }

    /**
     * Wie createGroup, nur das hier die Gruppe auch als Veranstaltung gesetzt werden kann und CSV Dateien mit Nutzern
     * eingelesen werden können.
     *
     * @param account             Der Nutzer der die Gruppe erstellt
     * @param title               Parameter für die neue Gruppe
     * @param description         Parameter für die neue Gruppe
     * @param isVisibilityPrivate Parameter für die neue Gruppe
     * @param isLecture           Parameter für die neue Gruppe
     * @param isMaximumInfinite   Parameter für die neue Gruppe
     * @param userMaximum         Parameter für die neue Gruppe
     * @param parent              Parameter für die neue Gruppe
     * @param file                Parameter für die neue Gruppe
     */
    //TODO: remove booleans + add wrapper?
    //TODO: auslagern teilweise -> EventBuilderService
    public void createGroupAsOrga(Account account,
                                  String title,
                                  String description,
                                  Boolean isVisibilityPrivate,
                                  Boolean isLecture,
                                  Boolean isMaximumInfinite,
                                  Long userMaximum,
                                  UUID parent,
                                  MultipartFile file) {

        userMaximum = GroupService.checkInfiniteUsers(isMaximumInfinite, userMaximum);

        List<User> newUsers = CsvService.readCsvFile(file);

        List<User> oldUsers = new ArrayList<>();
        User user = new User(account);
        oldUsers.add(user);

        GroupService.removeOldUsersFromNewUsers(oldUsers, newUsers);

        userMaximum = GroupService.adjustUserMaximum((long) newUsers.size(), 1L, userMaximum);

        UUID groupId = createGroup(account,
                                   title,
                                   description,
                                   isVisibilityPrivate,
                                   isLecture,
                                   isMaximumInfinite,
                                   userMaximum, parent);

        groupService.addUserList(newUsers, groupId);
    }

    /**
     * Erzeugt eine neue Gruppe, fügt den User, der die Gruppe erstellt hat, hinzu und setzt seine Rolle als Admin fest.
     * Zudem wird der Gruppentitel und die Gruppenbeschreibung erzeugt, welche vorher der Methode übergeben wurden.
     * Aus diesen Event-Objekten wird eine Liste erzeugt, welche daraufhin mithilfe des EventServices gesichert wird.
     *
     * @param account     Keycloak-Account
     * @param title       Gruppentitel
     * @param description Gruppenbeschreibung
     */
    //TODO: remove booleans + add wrapper?
    //TODO: auslagern teilweise -> EventBuilderService
    public UUID createGroup(Account account,
                            String title,
                            String description,
                            Boolean isVisibilityPrivate,
                            Boolean isLecture,
                            Boolean isMaximumInfinite,
                            Long userMaximum,
                            UUID parent) {

        userMaximum = GroupService.checkInfiniteUsers(isMaximumInfinite, userMaximum);

        Visibility groupVisibility = GroupService.setGroupVisibility(isVisibilityPrivate);
        UUID groupId = UUID.randomUUID();

        GroupType groupType = GroupService.setGroupType(isLecture);

        CreateGroupEvent createGroupEvent = new CreateGroupEvent(groupId,
                                                                 account.getName(),
                                                                 parent,
                                                                 groupType,
                                                                 groupVisibility,
                                                                 userMaximum);
        eventStoreService.saveEvent(createGroupEvent);

        inviteService.createLink(groupId);

        User user = new User(account.getName(), "", "", "");

        groupService.addUser(account, groupId);
        groupService.updateTitle(account, groupId, title);
        groupService.updateDescription(account, groupId, description);
        groupService.updateRole(user, groupId);

        return groupId;
    }

}
