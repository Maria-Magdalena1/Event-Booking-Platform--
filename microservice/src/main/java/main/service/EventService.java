package main.service;

import lombok.RequiredArgsConstructor;
import main.entity.Event;
import main.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public void save(Event event) {
        eventRepository.save(event);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Object count() {
        return eventRepository.count();
    }
}
