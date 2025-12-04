package main.scheduler;

import lombok.extern.slf4j.Slf4j;
import main.services.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventScheduler {
    private final EventService eventService;

    public EventScheduler(EventService eventService) {
        this.eventService = eventService;
    }

    @Scheduled(fixedRate = 86_400_000, initialDelay = 10_000)
    public void dailyCleanup() {
        eventService.removeExpiredEvents();
    }

    @Scheduled(cron = "0 0/5 * * * *", zone = "Europe/Sofia")
    public void preWarmUpcomingEventsCache() {
        int size = eventService.getUpcomingEvents().size();
        log.info("Cache refreshed. Upcoming events = {}", size);
    }

}
