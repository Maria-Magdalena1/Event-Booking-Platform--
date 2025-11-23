package main.scheduler;

import main.services.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        eventService.getUpcomingEvents();
    }

}
