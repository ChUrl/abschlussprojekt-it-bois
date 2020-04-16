package mops.gruppen2.persistance;

import mops.gruppen2.persistance.dto.EventDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventDTO, Long> {


    // ####################################### EVENT DTOs ########################################


    @Query("SELECT * FROM heroku_f6ff902475fc2fa")
    List<EventDTO> findAllEvents();
}
