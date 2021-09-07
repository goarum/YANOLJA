package yanolja;

import yanolja.config.kafka.KafkaProcessor;

import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_ReserveCheck(@Payload Reserved reserved){

        try {
            if (!reserved.validate()) return;

            System.out.println("\n\n##### listener ReserveCheck : " + reserved.toJson() + "\n\n");
                
            Optional<Room> roomOptional = roomRepository.findById(reserved.getId());

            if( roomOptional.isPresent()) {
                 Room room = roomOptional.get();

                 room.setState("Reserved");
                
                 roomRepository.save(room);
                }


        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelReserved_ReserveCancel(@Payload CancelReserved cancelReserved){

        try {
            if (!cancelReserved.validate()) return;

            System.out.println("\n\n##### listener ReserveCheck : " + cancelReserved.toJson() + "\n\n");
                
            Optional<Room> roomOptional = roomRepository.findById(cancelReserved.getId());

            if( roomOptional.isPresent()) {
                 Room room = roomOptional.get();
            
                 room.setState("공실");
                
                 roomRepository.save(room);
                }


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}