package yanolja;

import yanolja.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomAdded_RoomAdd(@Payload RoomAdded roomAdded){
        try {

            if (!roomAdded.validate()) return;

            System.out.println("\n\n##### listener RoomAdd : " + roomAdded.toJson() + "\n\n");


            Reservation reservation = new Reservation();

            reservation.setId(roomAdded.getId());
            reservation.setRoomType(roomAdded.getRoomType());
            reservation.setState("공실");
            reservation.setPrice(roomAdded.getPrice());
            reservation.setRoomId(roomAdded.getRoomId());
            reservation.setPaymentId(roomAdded.getPaymentId());

            reservationRepository.save(reservation);

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}