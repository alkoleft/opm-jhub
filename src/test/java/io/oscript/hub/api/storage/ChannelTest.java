package io.oscript.hub.api.storage;

import io.oscript.hub.api.exceptions.OperationFailedException;
import io.oscript.hub.api.utils.VersionComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChannelTest {

    IStoreProvider provider;
    Channel channel;

    static final String packageName = "package1";

    @BeforeEach
    void setUp() {
        provider = mock(IStoreProvider.class);
        channel = new Channel(new ChannelInfo("test"));
        channel.storeProvider = provider;
    }

    @Test
    void getPackages() throws IOException, OperationFailedException {

        var package1 = new StoredPackageInfo();
        package1.name = packageName;
        var package2 = new StoredPackageInfo();
        package2.name = "package2";
        when(provider.getPackages("test")).thenReturn(Arrays.asList(package1, package2));
        var result = channel.getPackages();
        assertThat(result).contains(package1).contains(package2);
    }

    @Test
    void getPackage() throws IOException {
        var package1 = new StoredPackageInfo();
        package1.name = packageName;
        when(provider.getPackage("test", packageName)).thenReturn(package1);
        var result = channel.getPackage(packageName);
        assertThat(result).isEqualTo(package1);
        result = channel.getPackage("package2");
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @CsvSource({"2.0.1, true",
            "2.0.0, false",
            "2.0.0.0, false",
            "1.0.0, false"
    })
    void pushPackage(String version, boolean packageUpdated) throws IOException {
        final String channelName = channel.getChannelInfo().getName();

        StoredPackageInfo storedPack = StoredPackageInfo.create(Metadata.create(packageName, "2.0.0"));
        StoredVersionInfo storedVersion = StoredVersionInfo.create(Metadata.create(packageName, version));

        when(provider.saveVersionBin(any())).thenReturn(true);
        when(provider.saveVersion(any())).thenReturn(true);
        when(provider.getPackage(channelName, packageName)).thenReturn(storedPack);
        when(provider.getVersion(channelName, packageName, version)).thenReturn(storedVersion);

        var pack = mock(SavingPackage.class);
        when(pack.getChannel()).thenReturn(channelName);
        when(pack.getName()).thenReturn(packageName);
        when(pack.getVersion()).thenReturn(version);

        var result = channel.pushPackage(pack);

        verify(channel.storeProvider).saveVersionBin(any());
        verify(channel.storeProvider).saveVersion(any());
        if (packageUpdated)
            verify(channel.storeProvider).savePackage(channelName, storedPack);
        else
            verify(channel.storeProvider, never()).savePackage(channelName, storedPack);
        assertThat(result).isEqualTo(storedVersion);
    }

    @Test
    void getVersions() throws IOException, OperationFailedException {
        when(provider.getVersions(any(), any())).thenReturn(List.of(
                StoredVersionInfo.create(Metadata.create(packageName, "2.0.0")),
                StoredVersionInfo.create(Metadata.create(packageName, "1.0.0")),
                StoredVersionInfo.create(Metadata.create(packageName, "2.0.4.6")),
                StoredVersionInfo.create(Metadata.create(packageName, "1.0.4"))
        ));
        var result = channel.getVersions(packageName);
        assertThat(result)
                .isNotEmpty()
                .isSortedAccordingTo((o1, o2) -> -VersionComparator.compare(o1.getVersion(), o2.getVersion()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.0.0", "", "latest"})
    @NullSource
    void getVersion(String incomingVersion) throws IOException {
        String version = "2.0.0";
        when(provider.getVersion(channel.getName(), packageName, version)).thenReturn(
                StoredVersionInfo.create(Metadata.create(packageName, version))
        );
        when(provider.getPackage(channel.getName(), packageName)).thenReturn(
                StoredPackageInfo.create(Metadata.create(packageName, version))
        );

        var result = channel.getVersion(packageName, incomingVersion);

        assertThat(result)
                .isNotNull()
                .extracting(StoredVersionInfo::getVersion)
                .isEqualTo(version);
    }
}