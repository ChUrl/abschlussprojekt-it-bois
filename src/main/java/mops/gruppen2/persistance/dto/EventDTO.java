package mops.gruppen2.persistance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Table("event")
@Getter
@AllArgsConstructor
public class EventDTO {

    @Id
    Long event_id; // Cache-Version

    String group_id;
    long group_version; // Group-Version

    String exec_id;
    String target_id;

    Timestamp event_date;
    String event_payload;
}
