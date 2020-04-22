package mops.gruppen2;

import mops.gruppen2.domain.event.Event;

import java.util.List;
import java.util.UUID;

public final class TestHelper {

    public static UUID uuid(int id) {
        String num = String.valueOf(id);
        String string = "00000000-0000-0000-0000-";
        string += "0".repeat(12 - num.length());
        string += num;

        return UUID.fromString(string);
    }

    public static void initEvents(List<Event> events) {
        for (int i = 1; i <= events.size(); i++) {
            events.get(i - 1).init(i);
        }
    }
}
