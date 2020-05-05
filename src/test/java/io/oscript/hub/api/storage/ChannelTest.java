package io.oscript.hub.api.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelTest {

    IStoreProvider provider;
    Channel channel;

    @BeforeEach
    void setUp() {
        provider = mock(IStoreProvider.class);
        channel = new Channel(new ChannelInfo("test", false));
        channel.storeProvider = provider;
    }

    @Test
    void getPackages() throws Exception {

        var package1 = new StoredPackageInfo();
        package1.name = "package1";
        var package2 = new StoredPackageInfo();
        package2.name = "package2";
        when(provider.getPackages("test")).thenReturn(Arrays.asList(package1, package2));
        var result = channel.getPackages();
        assertThat(result).contains(package1).contains(package2);
    }

    @Test
    void getPackage() throws IOException {
        var package1 = new StoredPackageInfo();
        package1.name = "package1";
        when(provider.getPackage("test", "package1")).thenReturn(package1);
        var result = channel.getPackage("package1");
        assertThat(result).isEqualTo(package1);
        result = channel.getPackage("package2");
        assertThat(result).isNull();
    }

    @Test
    void pushPackage() {
    }

    @Test
    void getVersions() {
    }

    @Test
    void getVersion() {
    }

    @Test
    void containsVersion() {
    }

    @Test
    void containsPackage() {
    }

    @Test
    void getVersionData() {
    }
}