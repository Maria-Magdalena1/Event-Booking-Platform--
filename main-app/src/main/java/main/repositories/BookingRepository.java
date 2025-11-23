package main.repositories;

import main.entities.Booking;
import main.entities.Event;
import main.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser(User user);

    List<Booking> findAllByEvent(Event event);
}
