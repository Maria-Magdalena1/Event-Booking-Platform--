package main.repositories;

import main.entities.Event;
import main.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE e.startDate > :now")
    List<Event> findAllUpcoming(@Param("now") LocalDateTime now);

    List<Event> findAllByCreator(User creator);

    List<Event> findAllByEndDateBeforeAndArchivedFalse(LocalDateTime now);

    @Query("""
               SELECT e FROM Event e
               WHERE e.startDate > :now AND e.archived = false
            """)
    List<Event> findAllUpcomingNotArchived(LocalDateTime now);
}
