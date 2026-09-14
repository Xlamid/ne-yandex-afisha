package git.xlamid.eventnotificatorservice.notification.controller;

import git.xlamid.eventnotificatorservice.notification.dto.GetNotificationDto;
import git.xlamid.eventnotificatorservice.notification.dto.MarkNotificationDto;
import git.xlamid.eventnotificatorservice.notification.service.EventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class EventNotificationController {

    private final EventNotificationService eventNotificationService;

    @GetMapping
    public ResponseEntity<List<GetNotificationDto>> getUnreadNotifications() {
        log.info("get unread notifications");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventNotificationService.getUnreadNotifications());
    }

    @PostMapping
    public ResponseEntity<Void> markNotificationsAsRead(@RequestBody MarkNotificationDto dto) {
        log.info("mark notifications as read: {}", dto.getNotificationIds());
        eventNotificationService.markNotificationsAsRead(dto);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}