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


    @Query("SELECT * FROM event WHERE event_id > :version AND event_id <= :max")
    List<EventDTO> findNewEvents(@Param("version") long version,
                                 @Param("max") long maxid);

    @Query("SELECT * FROM event")
    List<EventDTO> findAllEvents();


    // ################################ LATEST EVENT DTOs ########################################


    @Query("WITH ranked_events AS ("
           + "SELECT *, ROW_NUMBER() OVER (PARTITION BY group_id ORDER BY event_id DESC) AS rn"
           + " FROM event"
           + " WHERE target_id = :userId AND event_type IN ('ADDMEMBER', 'KICKMEMBER')"
           + ")"
           + "SELECT * FROM ranked_events WHERE rn = 1;")
    List<EventDTO> findLatestEventDTOsPartitionedByGroupTarget(@Param("userId") String target);

    @Query("WITH ranked_events AS ("
           + "SELECT *, ROW_NUMBER() OVER (PARTITION BY group_id ORDER BY event_id DESC) AS rn"
           + " FROM event"
           + " WHERE event_type IN (:types)"
           + ")"
           + "SELECT * FROM ranked_events WHERE rn = 1;")
    List<EventDTO> findLatestEventDTOsPartitionedByGroupByType(@Param("types") List<String> types);


    // ######################################### COUNT ###########################################


    @Query("SELECT MAX(event_id) FROM event")
    Long findMaxEventId();
}
