package mops.gruppen2.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ApplicationInit {

    private final GroupCache groupCache;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        groupCache.init();
    }
}
