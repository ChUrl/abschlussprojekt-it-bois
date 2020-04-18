package mops.gruppen2;

import java.util.UUID;

public final class TestHelper {

    public static UUID uuid(int id) {
        String num = String.valueOf(id);
        String string = "00000000-0000-0000-0000-";
        string += "0".repeat(12 - num.length());
        string += num;

        return UUID.fromString(string);
    }
}
