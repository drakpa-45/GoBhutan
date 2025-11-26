package com.goBhutan.adminPanel.busAdmin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatUpdate(Long scheduleId) {
        messagingTemplate.convertAndSend(
                "/topic/seats/" + scheduleId,
                Map.of("scheduleId", scheduleId, "updated", true)
        );
    }
}
