package ru.v0rt3x.perimeter.server.utils;

public class NetCalc {

    public static long ip2Long(String address) {
        long res = 0;
        String[] octets = address.split("\\.");

        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(octets[3 - i]);
            res |= ip << (8 * i);
        }

        return res;
    }

    public static String long2Ip(long address) {
        return ((address >> 24) & 0xFF) + "."
            + ((address >> 16) & 0xFF) + "."
            + ((address >> 8) & 0xFF) + "."
            + (address & 0xFF);
    }

    public static long getAddressCount(int cidr) {
        return Math.round(Math.pow(2, 32 - cidr));
    }

    public static String getSubnet(String baseNetwork, int cidr, int offset) {
        if (offset <= 0)
            throw new IllegalArgumentException("Offset should be positive number");

        if (offset == 1)
            return baseNetwork;

        return long2Ip(ip2Long(baseNetwork) + getAddressCount(cidr) * (offset - 1));
    }

    public static String getAddress(String subnet, int cidr, int offset) {
        if (offset > getAddressCount(cidr))
            throw new IllegalArgumentException("Address should belong to given network");

        return long2Ip(ip2Long(subnet) + offset - 1);
    }
}
