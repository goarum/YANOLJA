package yanolja;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationviewRepository extends CrudRepository<Reservationview, Long> {

    //Optional<Reservationview> findById(Long Id);

}