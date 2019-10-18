package utils;

import java.util.Arrays;

public class ArrayUtils {

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }
}
