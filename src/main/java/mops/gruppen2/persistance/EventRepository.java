package mops.gruppen2.persistance;

import mops.gruppen2.persistance.dto.EventDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventDTO, Long> {

    // ####################################### GROUP IDs #########################################

    /*@Query("SELECT DISTINCT group_id FROM event"
           + " WHERE user_id = :userId AND event_type = :type")
    List<String> findGroupIdsByUserAndType(@Param("userId") String userId,
                                           @Param("type") String type);*/

    @Query("SELECT DISTINCT group_id FROM event"
           + " WHERE event_id > :status")
    List<String> findGroupIdsWhereEventIdGreaterThanStatus(@Param("status") long status);

    // ####################################### EVENT DTOs ########################################

    @Query("SELECT * FROM event"
           + " WHERE group_id IN (:groupIds) ")
    List<EventDTO> findEventDTOsByGroup(@Param("groupIds") List<String> groupIds);

    /*@Query("SELECT * FROM event"
           + " WHERE group_id IN (:userIds) ")
    List<EventDTO> findEventDTOsByUser(@Param("groupIds") String... userIds);*/

    @Query("SELECT * FROM event"
           + " WHERE event_type IN (:types)")
    List<EventDTO> findEventDTOsByType(@Param("types") List<String> types);

    @Query("SELECT * FROM event"
           + " WHERE event_type IN (:types) AND group_id IN (:groupIds)")
    List<EventDTO> findEventDTOsByGroupAndType(@Param("groupIds") List<String> groupIds,
                                               @Param("types") List<String> types);

    /*@Query("SELECT * FROM event"
           + " WHERE event_type IN (:types) AND user_id = :userId")
    List<EventDTO> findEventDTOsByUserAndType(@Param("userId") String userId,
                                              @Param("types") String... types);*/

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
