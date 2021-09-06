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
    @Autowired RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_ReserveCheck(@Payload Reserved reserved){

        if(!reserved.validate()) return;

        System.out.println("\n\n##### listener ReserveCheck : " + reserved.toJson() + "\n\n");



        // Sample Logic //
        // Room room = new Room();
        // roomRepository.save(room);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelReserved_ReserveCancel(@Payload CancelReserved cancelReserved){

        if(!cancelReserved.validate()) return;

        System.out.println("\n\n##### listener ReserveCancel : " + cancelReserved.toJson() + "\n\n");



        // Sample Logic //
        // Room room = new Room();
        // roomRepository.save(room);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}