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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelReserved_Refund(@Payload CancelReserved cancelReserved){

        if(!cancelReserved.validate()) return;

        System.out.println("\n\n##### listener Refund : " + cancelReserved.toJson() + "\n\n");

        Long Id = cancelReserved.getId();
        Payment payment = paymentRepository.findByWtbId(Id);
        System.out.println("**** Refund "+ payment.getPrice() + " to reservation " + Id + "is Canceled ****" );
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomOffered_SendToRoom(@Payload RoomOffered roomOffered){

        if(!roomOffered.validate()) return;

        System.out.println("\n\n##### listener SendToRoom : " + roomOffered.toJson() + "\n\n");

        Long Id = roomOffered.getId();
        Payment payment = paymentRepository.findByWtbId(Id);
        System.out.println("**** Send To Seller "+ payment.getPrice() + "  to Room " + Id + "is Offered ****" );
   

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}