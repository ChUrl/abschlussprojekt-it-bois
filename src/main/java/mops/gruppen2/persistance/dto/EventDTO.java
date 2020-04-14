package mops.gruppen2.persistance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("event")
@Getter
@AllArgsConstructor
public class EventDTO {

    @Id
    Long event_id;

    String group_id;
    long group_version;

    String exec_id;
    String target_id;

    String event_type;
    String event_payload;
}
