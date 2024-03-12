package com.gorbatenko.budget.util;

import java.util.UUID;

public final class Utils {
    private Utils(){}

    public static final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static boolean equalsUUID(UUID first, UUID second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.compareTo(second) == 0;
    }

}
