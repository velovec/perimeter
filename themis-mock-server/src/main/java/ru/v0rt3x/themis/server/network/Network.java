package ru.v0rt3x.themis.server.network;

import java.util.ArrayList;
import java.util.List;

public class Network {

    private long firstAddress;
    private long lastAddress;

    private Integer cidr;

    private Network(String address, Integer cidr) {
        this.firstAddress = ip2Long(address);
        this.lastAddress = this.firstAddress + getAddressCount(cidr) - 1;
        this.cidr = cidr;
    }

    private Network(long firstAddress, Integer cidr) {
        this.firstAddress = firstAddress;
        this.lastAddress = this.firstAddress + getAddressCount(cidr) - 1;
        this.cidr = cidr;
    }

    public static Network fromString(String network) {
        if (!network.contains("/"))
            throw new IllegalArgumentException("Network should contain CIDR notation");

        String[] networkNotation = network.split("/");

        return fromCIDRNotatedString(networkNotation[0], Integer.parseInt(networkNotation[1]));
    }

    private static Network fromCIDRNotatedString(String network, int cidr) {
        return new Network(network, cidr);
    }

    private long ip2Long(String address) {
        long result = 0;

        String[] ipAddressInArray = address.split("\\.");

        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);

            result |= ip << (i * 8);
        }

        return result;
    }

    private String long2Ip(long address) {
        return ((address >> 24) & 0xFF) + "."
            + ((address >> 16) & 0xFF) + "."
            + ((address >> 8) & 0xFF) + "."
            + (address & 0xFF);
    }

    private long getAddressCount(int cidr) {
        return Math.round(Math.pow(2, 32 - cidr));
    }

    public boolean contains(String address) {
        long targetAddress = ip2Long(address);

        return this.firstAddress <= targetAddress && targetAddress <= this.lastAddress;
    }

    public List<Network> split(int count, int cidr) {
        long addressesAvailable = getAddressCount(this.cidr);
        long addressesRequired = count * getAddressCount(cidr);

        if (addressesAvailable < addressesRequired)
            throw new IllegalArgumentException(String.format(
                "Unable to split network: %s addresses required, but %s available",
                addressesAvailable, addressesRequired
            ));

        List<Network> networks = new ArrayList<>();
        long networkAddress = firstAddress;
        for (int i = 0; i < count; i++) {
            networks.add(new Network(networkAddress, cidr));
            networkAddress += getAddressCount(cidr);
        }
        return networks;
    }

    @Override
    public String toString() {
        return String.format("%s/%d", long2Ip(firstAddress), cidr);
    }
}
