package mops.gruppen2.domain.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.InvalidInviteException;
import mops.gruppen2.domain.exception.NoInviteExistException;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.persistance.InviteRepository;
import mops.gruppen2.persistance.dto.InviteLinkDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
public class InviteService {

    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    void createLink(Group group) {
        inviteRepository.save(new InviteLinkDTO(null,
                                                group.getGroupid().toString(),
                                                UUID.randomUUID().toString()));

        log.debug("Link wurde erzeugt! (Gruppe: {})", group.getGroupid());
    }

    void destroyLink(Group group) {
        inviteRepository.deleteLinkOfGroup(group.getGroupid().toString());

        log.debug("Link wurde zerstört! (Gruppe: {})", group.getGroupid());
    }

    public UUID getGroupIdFromLink(String link) {
        try {
            return UUID.fromString(inviteRepository.findGroupIdByLink(link));
        } catch (Exception e) {
            log.error("Gruppe zu Link ({}) konnte nicht gefunden werden!", link, e);
            throw new InvalidInviteException(link);
        }
    }

    public String getLinkByGroup(Group group) {
        try {
            return inviteRepository.findLinkByGroupId(group.getGroupid().toString());
        } catch (Exception e) {
            log.error("Link zu Gruppe ({}) konnte nicht gefunden werden!", group.getGroupid(), e);
            throw new NoInviteExistException(group.getGroupid().toString());
        }
    }
}
