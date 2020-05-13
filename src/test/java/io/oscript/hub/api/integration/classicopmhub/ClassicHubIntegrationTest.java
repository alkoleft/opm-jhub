package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.exceptions.OperationFailedException;
import io.oscript.hub.api.storage.Channel;
import io.oscript.hub.api.storage.JSONSettingsProvider;
import io.oscript.hub.api.storage.SavingPackage;
import io.oscript.hub.api.storage.Storage;
import io.oscript.hub.api.storage.StoredVersionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassicHubIntegrationTest {

    ClassicHubIntegration hubIntegration;
    static final String hubIO = "http://hub.oscript.io";
    static final String hubRU = "http://hub.oscript.ru";

    @BeforeEach
    void setUp() {
        hubIntegration = new ClassicHubIntegration();
        hubIntegration.config = ClassicHubConfiguration.defaultConfiguration();
    }

    @Test
    void initialize() throws IOException {
        var settings = mock(JSONSettingsProvider.class);

        hubIntegration.settings = settings;
        hubIntegration.store = mock(Storage.class);
        hubIntegration.initialize();
        verify(settings).getConfiguration("opm-hub-mirror", ClassicHubConfiguration.class);
        verify(settings).saveConfiguration(eq("opm-hub-mirror"), any());
        verify(hubIntegration.store).registrationChannel("stable");
    }

    @Test
    void sync() throws OperationFailedException {
        hubIntegration.settings = mock(JSONSettingsProvider.class);
        when(hubIntegration.settings.saveConfiguration(any(), any())).thenReturn(true);

        var hub = spy(hubIntegration);

        doReturn(List.of("opm", "ClientSSH", "zoo").stream()).when(hub).loadPackageIDs(hubIO);
        doReturn(List.of("opm", "asserts", "fs", "asserts").stream()).when(hub).loadPackageIDs(hubRU);
        doReturn(null).when(hub).loadVersions(any());
        doReturn(List.of(new PackageVersion("1.0", "opm")))
                .when(hub)
                .loadVersions(argThat(argument -> argument.getName().equals("opm")));
        doNothing().when(hub).downloadPackages();

        doCallRealMethod().when(hub).sync();

        hub.sync();

        verify(hub, times(2)).loadPackageIDs(any());

        assertThat(hubIntegration.packages)
                .doesNotHaveDuplicates()
                .hasSize(5)
                .extracting(Package::getName)
                .contains("opm")
                .contains("asserts");

        assertThat(hubIntegration.versions)
                .hasSize(1);
    }

    @Test
    void downloadPackages() {
        var channel = mock(Channel.class);

        var pack = testPackage(true);
        var version = testVersion(true);
        pack.versions.add(version);
        hubIntegration.packages.add(pack);
        hubIntegration.versions.add(version);
        hubIntegration.packages.add(testPackage(true));
        hubIntegration.packages.add(testPackage(false));
        hubIntegration.versions.add(testVersion(false));
        hubIntegration.mainChannel = channel;

        when(channel.containsVersion(eq(version.packageID), any())).thenReturn(false);
        when(channel.pushPackage(any())).thenReturn(new StoredVersionInfo());

        hubIntegration.downloadPackages();

        verify(channel, times(2)).pushPackage(Mockito.any());
    }

    @Test
    void downloadVersion() {
        var requestVersion = testVersion(true);
        var result = hubIntegration.downloadVersion(requestVersion);

        assertThat(result)
                .isNotNull()
                .extracting(SavingPackage::getName, SavingPackage::getVersion)
                .contains(requestVersion.getPackageID(), requestVersion.getVersion());
    }

    @Test
    void downloadLastVersions() {
        var requestPackage = testPackage(true);
        var result = hubIntegration.downloadLastVersion(requestPackage);

        assertThat(result)
                .isNotNull()
                .matches(savingPackage -> Objects.equals(savingPackage.getName(), requestPackage.getName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {hubIO, hubRU})
    void loadPackageIDs(String server) {
        List<String> result = hubIntegration.loadPackageIDs(server).collect(Collectors.toList());

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .doesNotHaveDuplicates()
                .contains("opm")
                .contains("v8unpack")
                .contains("vanessa-runner");
    }

    @Test
    void loadVersions() {
        var result = hubIntegration.loadVersions(testPackage(true));
        assertThat(result).isNotNull()
                .doesNotContainNull()
                .extracting(PackageVersion::getVersion)
                .doesNotHaveDuplicates()
                .contains("0.16.2")
                .contains("0.16.0")
                .contains("0.2.1");
    }

    @Test
    void versions() {
        String server = ClassicHubConfiguration.defaultConfiguration().servers.get(0);
        var result = ClassicHubIntegration.versionsFromPackagePage(testPackage(true), server);
        assertThat(result).isNotNull()
                .doesNotContainNull()
                .contains("0.16.2")
                .contains("0.16.0")
                .contains("0.2.1");
    }

    @Test
    void versionsFromDownload() {
        String server = ClassicHubConfiguration.defaultConfiguration().servers.get(1);
        var result = ClassicHubIntegration.versionsFromDownload(testPackage(true), server);
        assertThat(result).isNotNull()
                .doesNotContainNull()
                .contains("0.16.2")
                .contains("0.16.0")
                .contains("0.2.1");
    }

    static Package testPackage(boolean exists) {
        return new Package(exists ? "opm" : "unknown-package");
    }

    static PackageVersion testVersion(boolean exists) {
        return new PackageVersion(exists ? "0.16.2" : "999999", "opm");
    }

}