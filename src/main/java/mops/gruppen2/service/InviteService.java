package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.dto.InviteLinkDTO;
import mops.gruppen2.domain.exception.InvalidInviteException;
import mops.gruppen2.domain.exception.NoInviteExistException;
import mops.gruppen2.repository.InviteRepository;
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
                                                group.getId().toString(),
                                                UUID.randomUUID().toString()));

        log.debug("Link wurde erzeugt! (Gruppe: {})", group.getId());
    }

    void destroyLink(Group group) {
        inviteRepository.deleteLinkOfGroup(group.getId().toString());

        log.debug("Link wurde zerstört! (Gruppe: {})", group.getId());
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
            return inviteRepository.findLinkByGroupId(group.getId().toString());
        } catch (Exception e) {
            log.error("Link zu Gruppe ({}) konnte nicht gefunden werden!", group.getId(), e);
            throw new NoInviteExistException(group.getId().toString());
        }
    }
}
