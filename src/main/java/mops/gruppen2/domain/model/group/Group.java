package mops.gruppen2.domain.model.group;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.exception.LastAdminException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.wrapper.Body;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.domain.model.group.wrapper.Parent;
import mops.gruppen2.domain.model.group.wrapper.Title;
import mops.gruppen2.domain.service.helper.CommonHelper;
import mops.gruppen2.domain.service.helper.SortHelper;
import mops.gruppen2.domain.service.helper.ValidationHelper;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repräsentiert den aggregierten Zustand einer Gruppe.
 *
 * <p>
 * Muss beim Start gesetzt werden: groupid, meta
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Group {

    // Metainformationen
    @EqualsAndHashCode.Include
    private UUID groupid;

    @Getter
    private Type type = Type.PRIVATE;

    private Parent parent = Parent.EMPTY();

    private Limit limit = Limit.DEFAULT(); // Add initial user

    private Link link = Link.RANDOM();

    private GroupMeta meta = GroupMeta.EMPTY();

    private GroupOptions options = GroupOptions.DEFAULT();

    // Inhalt
    private Title title = Title.EMPTY();

    private Description description = Description.EMPTY();

    private Body body;

    // Integrationen

    // Teilnehmer
    private Map<String, Membership> memberships = new HashMap<>();


    // ####################################### Members ###########################################


    public List<User> getMembers() {
        return SortHelper.sortByMemberRole(new ArrayList<>(memberships.values())).stream()
                         .map(Membership::getUser)
                         .collect(Collectors.toList());
    }

    public List<User> getRegulars() {
        return memberships.values().stream()
                          .map(Membership::getUser)
                          .filter(member -> isRegular(member.getId()))
                          .collect(Collectors.toList());
    }

    public List<User> getAdmins() {
        return memberships.values().stream()
                          .map(Membership::getUser)
                          .filter(member -> isAdmin(member.getId()))
                          .collect(Collectors.toList());
    }

    public Role getRole(String userid) {
        return memberships.get(userid).getRole();
    }

    public void addMember(String target, User user) throws UserAlreadyExistsException, GroupFullException {
        ValidationHelper.throwIfMember(this, target);
        ValidationHelper.throwIfGroupFull(this);

        memberships.put(target, new Membership(user, Role.REGULAR));
    }

    public void kickMember(String target) throws UserNotFoundException, LastAdminException {
        ValidationHelper.throwIfNoMember(this, target);
        ValidationHelper.throwIfLastAdmin(this, target);

        memberships.remove(target);
    }

    public boolean memberHasRole(String target, Role role) {
        ValidationHelper.throwIfNoMember(this, target);

        return memberships.get(target).getRole() == role;
    }

    public void memberPutRole(String target, Role role) throws UserNotFoundException, LastAdminException {
        ValidationHelper.throwIfNoMember(this, target);
        if (role == Role.REGULAR) {
            ValidationHelper.throwIfLastAdmin(this, target);
        }

        memberships.put(target, memberships.get(target).setRole(role));
    }

    public boolean isMember(String target) {
        return memberships.containsKey(target);
    }

    public boolean isAdmin(String target) throws UserNotFoundException {
        ValidationHelper.throwIfNoMember(this, target);

        return memberships.get(target).getRole() == Role.ADMIN;
    }

    public boolean isRegular(String target) throws UserNotFoundException {
        ValidationHelper.throwIfNoMember(this, target);

        return memberships.get(target).getRole() == Role.REGULAR;
    }


    // ######################################### Getters #########################################


    public UUID getId() {
        return groupid;
    }

    public UUID getParent() {
        return parent.getValue();
    }

    public long getLimit() {
        return limit.getValue();
    }

    public String getTitle() {
        return title.toString();
    }

    public String getDescription() {
        return description.getValue();
    }

    public String getLink() {
        return link.getValue();
    }

    public String creator() {
        return meta.getCreator();
    }

    public long version() {
        return meta.getVersion();
    }

    public LocalDateTime creationDate() {
        return meta.getCreationDate();
    }

    public int size() {
        return memberships.size();
    }

    public boolean isFull() {
        return size() >= limit.getValue();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean exists() {
        return groupid != null && !CommonHelper.uuidIsEmpty(groupid);
    }

    public boolean isPublic() {
        return type == Type.PUBLIC;
    }

    public boolean isPrivate() {
        return type == Type.PRIVATE;
    }

    public boolean isLecture() {
        return type == Type.LECTURE;
    }

    public boolean hasParent() {
        return !parent.isEmpty();
    }


    // ######################################## Setters ##########################################


    public void setId(UUID groupid) throws BadArgumentException {
        if (this.groupid != null) {
            throw new BadArgumentException("GruppenId bereits gesetzt.");
        }

        this.groupid = groupid;
    }

    public void setType(String exec, Type type) throws NoAccessException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        this.type = type;
    }

    public void setTitle(String exec, @Valid Title title) throws NoAccessException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        this.title = title;
    }

    public void setDescription(String exec, @Valid Description description) throws NoAccessException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        this.description = description;
    }

    public void setLimit(String exec, @Valid Limit limit) throws NoAccessException, BadArgumentException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        if (size() > limit.getValue()) {
            throw new BadArgumentException("Das Userlimit ist zu klein für die Gruppe.");
        }

        this.limit = limit;
    }

    public void setParent(String exec, @Valid Parent parent) throws NoAccessException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        this.parent = parent;
    }

    public void setLink(String exec, @Valid Link link) throws NoAccessException {
        ValidationHelper.throwIfNoAdmin(this, exec);

        this.link = link;
    }

    public void updateVersion(long version) throws IdMismatchException {
        meta = meta.setVersion(version);
    }

    public void setCreator(String target) throws BadArgumentException {
        meta = meta.setCreator(target);
    }

    public void setCreationDate(LocalDateTime date) throws BadArgumentException {
        meta = meta.setCreationDate(date);
    }


    // ######################################### Util ############################################


    public void destroy(String userid) throws NoAccessException {
        if (!isEmpty()) {
            ValidationHelper.throwIfNoAdmin(this, userid);
        }

        groupid = null;
        type = null;
        parent = null;
        limit = null;
        link = null;
        meta = null;
        options = null;
        title = null;
        description = null;
        body = null;
        memberships = null;
    }

    public String format() {
        return type + ": " + title + " - " + description;
    }

    @Override
    public String toString() {
        return "group("
               + (groupid == null ? "groupid: null" : groupid.toString())
               + ", "
               + (parent == null ? "parent: null" : parent.toString())
               + ", "
               + (meta == null ? "meta: null" : meta.toString())
               + ")";
    }

    public static Group EMPTY() {
        return new Group();
    }
}
