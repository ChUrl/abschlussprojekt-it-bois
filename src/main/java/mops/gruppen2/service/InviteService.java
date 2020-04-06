package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
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

    void createLink(UUID groupId) {
        inviteRepository.save(new InviteLinkDTO(null, groupId.toString(), UUID.randomUUID().toString()));
    }

    void destroyLink(UUID groupId) {
        inviteRepository.deleteLinkOfGroup(groupId.toString());
    }

    public UUID getGroupIdFromLink(String link) {
        try {
            return UUID.fromString(inviteRepository.findGroupIdByLink(link));
        } catch (Exception e) {
            log.error("Gruppe zu Link ({}) konnte nicht gefunden werden!", link);
            throw new InvalidInviteException(link);
        }
    }

    public String getLinkByGroupId(UUID groupId) {
        try {
            return inviteRepository.findLinkByGroupId(groupId.toString());
        } catch (Exception e) {
            log.error("Link zu Gruppe ({}) konnte nicht gefunden werden!", groupId);
            throw new NoInviteExistException(groupId.toString());
        }
    }
}
