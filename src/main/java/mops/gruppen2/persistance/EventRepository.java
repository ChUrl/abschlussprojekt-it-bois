package mops.gruppen2.persistance;

import mops.gruppen2.persistance.dto.EventDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventDTO, Long> {


    // ####################################### EVENT DTOs ########################################


    @Query("SELECT * FROM event")
    List<EventDTO> findAllEvents();

    @Query("SELECT * FROM event WHERE group_id = :groupid")
    List<EventDTO> findGroupEvents(@Param("groupid") String groupId);
}
