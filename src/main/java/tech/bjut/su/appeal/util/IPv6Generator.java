package tech.bjut.su.appeal.util;

import java.util.Random;

public class IPv6Generator {
    public static String generateInternal() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("fe80:");

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                int val = random.nextInt(16);
                sb.append(Integer.toHexString(val));
            }

            if (i < 6) {
                sb.append(":");
            }
        }

        return sb.toString();
    }
}
