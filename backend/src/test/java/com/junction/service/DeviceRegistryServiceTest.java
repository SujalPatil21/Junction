package com.junction.service;

import com.junction.dto.DeviceDefinitionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceRegistryServiceTest {

    private DeviceRegistryService deviceRegistry;

    @BeforeEach
    void setUp() {
        deviceRegistry = new DeviceRegistryService();
    }

    @Test
    @DisplayName("Device registry initializes default multi-sensor network")
    void testGetAllDevices() {
        List<DeviceDefinitionDto> devices = deviceRegistry.getAll();
        assertThat(devices).hasSize(6);
        assertThat(devices).extracting(DeviceDefinitionDto::getId)
            .contains("DEV_CCTV_WANKHEDE_01", "DEV_CCTV_WANKHEDE_02", "DEV_CCTV_CHURCHGATE_01");
    }

    @Test
    @DisplayName("Filtering by zone returns only sensors assigned to that zone")
    void testGetByZone() {
        List<DeviceDefinitionDto> wankhedeSensors = deviceRegistry.getByZone("ZONE_WANKHEDE");
        assertThat(wankhedeSensors).isNotEmpty();
        assertThat(wankhedeSensors).allMatch(d -> "ZONE_WANKHEDE".equals(d.getZoneId()));
    }
}
