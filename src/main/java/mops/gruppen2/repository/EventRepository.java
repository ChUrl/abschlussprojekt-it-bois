package mops.gruppen2.repository;

import mops.gruppen2.domain.event.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Long> {
}