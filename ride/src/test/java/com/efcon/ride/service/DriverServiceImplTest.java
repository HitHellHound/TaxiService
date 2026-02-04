package com.efcon.ride.service;

import com.efcon.ride.dto.DriverRequest;
import com.efcon.ride.dto.DriverResponse;
import com.efcon.ride.external.client.DriverClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {
    @Mock
    private DriverClient driverClient;

    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    void getAllShouldUseDriverClient() {
        var driverResponse1 = Mockito.mock(DriverResponse.class);
        var driverResponse2 = Mockito.mock(DriverResponse.class);
        var driverResponse3 = Mockito.mock(DriverResponse.class);

        var driverResponseList = List.of(driverResponse1, driverResponse2, driverResponse3);
        Mockito.when(driverClient.getAll()).thenReturn(driverResponseList);

        driverService.getAll();

        Mockito.verify(driverClient).getAll();
    }

    @Test
    void getShouldUseDriverClient() {
        var driverId = 1L;
        var driverResponse = Mockito.mock(DriverResponse.class);
        Mockito.when(driverClient.get(driverId)).thenReturn(driverResponse);

        var result = driverService.get(driverId);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(driverClient).get(driverId);
    }

    @Test
    void createShouldUseDriverClient() {
        var driverRequest = Mockito.mock(DriverRequest.class);
        var driverResponse = Mockito.mock(DriverResponse.class);
        Mockito.when(driverClient.create(driverRequest)).thenReturn(driverResponse);

        var result = driverService.create(driverRequest);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(driverClient).create(driverRequest);
    }

    @Test
    void updateShouldUseDriverClient() {
        var driverId = 1L;
        var driverRequest = Mockito.mock(DriverRequest.class);
        var driverResponse = Mockito.mock(DriverResponse.class);
        Mockito.when(driverClient.update(driverId, driverRequest)).thenReturn(driverResponse);

        var result = driverService.update(driverId, driverRequest);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(driverClient).update(driverId, driverRequest);
    }

    @Test
    void deleteShouldUseDriverClient() {
        var driverId = 1L;

        driverService.delete(driverId);

        Mockito.verify(driverClient).delete(driverId);
    }
}