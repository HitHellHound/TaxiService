package com.efcon.ride.service;

import com.efcon.ride.broker.OutboundRideNotificationPublisher;
import com.efcon.ride.dao.RideNotificationDao;
import com.efcon.ride.dto.RideInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class RideNotificationServiceImplTest {
    @Mock
    private OutboundRideNotificationPublisher rideNotificationPublisher;
    @Mock
    private DriverInfoService driverInfoService;
    @Mock
    private RideNotificationDao rideNotificationDao;

    @InjectMocks
    private RideNotificationServiceImpl rideNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rideNotificationService, "notificationStaleAfter", 10);
    }

    @Test
    @SuppressWarnings("unchecked")
    void notifyDriversShouldCreateNotificationByDaoAndPostOutboundEvent() {
        var rideInfo = Mockito.mock(RideInfo.class);
        var driverIds = List.of(1L, 2L, 3L);
        Mockito.when(driverInfoService.getSomeFreeDriverIds(anyInt())).thenReturn(driverIds);
        ArgumentCaptor<Set<Long>> driverIdsSet = ArgumentCaptor.forClass(Set.class);

        rideNotificationService.notifyDrivers(rideInfo);

        Mockito.verify(rideNotificationDao).create(rideInfo);
        Mockito.verify(driverInfoService).getSomeFreeDriverIds(anyInt());
        Mockito.verify(rideNotificationPublisher).notifyDrivers(eq(rideInfo), driverIdsSet.capture());
        assertThat(driverIdsSet.getValue())
                .isNotEmpty()
                .containsAll(driverIds);
    }

    @Test
     void closeNotificationShouldUseDao() {
        var rideId = 1L;

        rideNotificationService.closeNotification(rideId);

        Mockito.verify(rideNotificationDao).closeNotificationById(rideId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void resendStaleNotificationsShouldFindStaleOpenedNotificationsAndPostOutboundEvent() {
        var rideInfo1 = Mockito.mock(RideInfo.class);
        Mockito.when(rideInfo1.rideId()).thenReturn(1L);

        var rideInfo2 = Mockito.mock(RideInfo.class);
        Mockito.when(rideInfo2.rideId()).thenReturn(2L);

        var rideInfoToResend = List.of(rideInfo1, rideInfo2);
        Mockito.when(rideNotificationDao.findStaleOpenedNotifications(anyInt())).thenReturn(rideInfoToResend);

        var driverIds = List.of(1L, 2L, 3L);
        Mockito.when(driverInfoService.getSomeFreeDriverIds(anyInt())).thenReturn(driverIds);

        ArgumentCaptor<List<Long>> rideInfoIds = ArgumentCaptor.forClass(List.class);

        rideNotificationService.resendStaleNotifications();

        Mockito.verify(rideNotificationDao).findStaleOpenedNotifications(anyInt());

        Mockito.verify(rideNotificationDao).updateLastNotifiedByIds(rideInfoIds.capture());
        assertThat(rideInfoIds.getValue())
                .isNotEmpty()
                .containsAll(rideInfoToResend
                        .stream()
                        .map(RideInfo::rideId)
                        .toList());

        Mockito.verify(rideNotificationPublisher, Mockito.times(rideInfoToResend.size()))
                .notifyDrivers(any(RideInfo.class), anySet());

        rideInfoToResend.forEach(rideInfo ->
            Mockito.verify(rideNotificationPublisher).notifyDrivers(eq(rideInfo), anySet()));

        Mockito.verify(driverInfoService, Mockito.times(rideInfoToResend.size())).getSomeFreeDriverIds(anyInt());
    }
}