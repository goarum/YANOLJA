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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelReserved_Refund(@Payload CancelReserved cancelReserved){


        try {
            if (!cancelReserved.validate()) return;

            System.out.println("\n\n##### listener ReserveCheck : " + cancelReserved.toJson() + "\n\n");
                
            Optional<Payment> paymentOptional = paymentRepository.findById(cancelReserved.getId());

            if( paymentOptional.isPresent()) {
                 Payment payment = paymentOptional.get();
            
                 payment.setId(cancelReserved.getId());

                 System.out.println("**** Refund "+ payment.getPrice() + "원 and reservation Room " + payment.getId() + " is Canceled ****" );
                
                 paymentRepository.save(payment);
                }


        }catch (Exception e){
            e.printStackTrace();
        }

            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomOffered_SendToRoom(@Payload RoomOffered roomOffered){

        try {
            if (!roomOffered.validate()) return;

            System.out.println("\n\n##### listener ReserveCheck : " + roomOffered.toJson() + "\n\n");
                
            //Optional<Payment> paymentOptional = paymentRepository.findByRoomId(roomOffered.getRoomId());

            //if( paymentOptional.isPresent()) {
            //     Payment payment = paymentOptional.get();
            Payment payment = paymentRepository.findByRoomId(roomOffered.getRoomId());
            payment.setRoomId(roomOffered.getRoomId());

            System.out.println("**** Send To Seller "+ payment.getPrice() + "원  to Room " + payment.getRoomId() + " ****" );
                
            paymentRepository.save(payment);
            //    }


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}