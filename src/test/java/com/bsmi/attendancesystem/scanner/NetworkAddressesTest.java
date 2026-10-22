package com.bsmi.attendancesystem.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Stream;

class NetworkAddressesTest {

    @Test
    void homeRouterAddressesComeFirstThenTenThenTheOtherPrivateRange() {
        List<Inet4Address> picked =
                NetworkAddresses.pick(addresses("172.16.0.5", "10.0.0.7", "192.168.1.20"));

        assertThat(picked)
                .extracting(InetAddress::getHostAddress)
                .containsExactly("192.168.1.20", "10.0.0.7", "172.16.0.5");
    }

    @Test
    void publicLoopbackLinkLocalAndIpv6AddressesAreLeftOut() {
        List<Inet4Address> picked =
                NetworkAddresses.pick(
                        addresses(
                                "8.8.8.8",
                                "127.0.0.1",
                                "169.254.10.1",
                                "fe80::1",
                                "fec0::1",
                                "192.168.1.20"));

        assertThat(picked).extracting(InetAddress::getHostAddress).containsExactly("192.168.1.20");
    }

    @Test
    void anAddressOnTwoInterfacesIsOfferedOnce() {
        List<Inet4Address> picked =
                NetworkAddresses.pick(addresses("192.168.1.20", "192.168.1.20"));

        assertThat(picked).hasSize(1);
    }

    @Test
    void noAddressesGiveAnEmptyList() {
        assertThat(NetworkAddresses.pick(List.of())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "docker0",
                "br-1a2b3c",
                "veth12ab",
                "virbr0",
                "podman1",
                "cni0",
                "vmnet8",
                "vboxnet0",
                "Docker0"
            })
    void containerAndVirtualMachineBridgesAreRecognised(String name) {
        assertThat(NetworkAddresses.isVirtualName(name)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"wlan0", "wlp3s0", "eth0", "enp0s31f6", "en0", "Wi-Fi"})
    void realNetworkCardsAreNotVirtual(String name) {
        assertThat(NetworkAddresses.isVirtualName(name)).isFalse();
    }

    @Test
    void thisComputersAddressesAreAllPrivateIpv4() {
        // Whatever network the test machine has, only reachable private IPv4 addresses come back
        assertThat(NetworkAddresses.forPhones()).allMatch(InetAddress::isSiteLocalAddress);
    }

    @Test
    void thisComputerOnlyIsTheLoopbackAddress() {
        Inet4Address loopback = NetworkAddresses.thisComputerOnly();

        assertThat(loopback.getHostAddress()).isEqualTo("127.0.0.1");
        assertThat(loopback.isLoopbackAddress()).isTrue();
    }

    private static List<InetAddress> addresses(String... literals) {
        // Number literals are parsed, never looked up in DNS
        return Stream.of(literals).map(NetworkAddressesTest::parse).toList();
    }

    private static InetAddress parse(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(literal, e);
        }
    }
}
