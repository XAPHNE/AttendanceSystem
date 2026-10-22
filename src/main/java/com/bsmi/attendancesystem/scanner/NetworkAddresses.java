package com.bsmi.attendancesystem.scanner;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Finds the address a phone on the same Wi-Fi can reach this computer at. */
public final class NetworkAddresses {
    // Bridges that container and VM tools add; a phone can't reach them
    private static final List<String> VIRTUAL_NAME_PREFIXES =
            List.of("docker", "br-", "veth", "virbr", "podman", "cni", "vmnet", "vboxnet");

    private NetworkAddresses() {}

    /**
     * @return 127.0.0.1, which only a browser on this computer can open
     */
    public static Inet4Address thisComputerOnly() {
        try {
            return (Inet4Address) InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1});
        } catch (UnknownHostException impossible) {
            // Only thrown for an address of the wrong length
            throw new IllegalStateException(impossible);
        }
    }

    /**
     * @return this computer's private IPv4 addresses, the likeliest Wi-Fi one first; may be empty
     */
    public static List<Inet4Address> forPhones() {
        List<InetAddress> found = new ArrayList<>();
        try {
            for (NetworkInterface network : NetworkInterface.networkInterfaces().toList()) {
                if (network.isUp()
                        && !network.isLoopback()
                        && !network.isVirtual()
                        && !isVirtualName(network.getName())) {
                    found.addAll(network.inetAddresses().toList());
                }
            }
        } catch (SocketException e) {
            // Nothing to offer; the screen then says no network was found
            return List.of();
        }
        return pick(found);
    }

    /** Keeps the private IPv4 addresses, home router ones (192.168.x.x) first, then 10.x.x.x. */
    static List<Inet4Address> pick(Collection<? extends InetAddress> addresses) {
        return addresses.stream()
                .filter(Inet4Address.class::isInstance)
                .map(Inet4Address.class::cast)
                .filter(InetAddress::isSiteLocalAddress)
                .distinct()
                .sorted(Comparator.comparingInt(NetworkAddresses::rank))
                .toList();
    }

    static boolean isVirtualName(String interfaceName) {
        String name = interfaceName.toLowerCase(Locale.ROOT);
        return VIRTUAL_NAME_PREFIXES.stream().anyMatch(name::startsWith);
    }

    private static int rank(Inet4Address address) {
        int firstByte = address.getAddress()[0] & 0xFF;
        return switch (firstByte) {
            case 192 -> 0;
            case 10 -> 1;
            default -> 2;
        };
    }
}
