package yanolja;

import yanolja.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationviewViewHandler {


    @Autowired
    private ReservationviewRepository reservationviewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomAdded_then_CREATE_1 (@Payload RoomAdded roomAdded) {
        try {

            if (!roomAdded.validate()) return;

            // view 객체 생성
            Reservationview reservationview = new Reservationview();
            // view 객체에 이벤트의 Value 를 set 함
            reservationview.setId(roomAdded.getId());
            reservationview.setRoomType(roomAdded.getRoomType());
            reservationview.setState("공실");
            reservationview.setPrice(roomAdded.getPrice());
            reservationview.setRoomId(roomAdded.getRoomId());
            reservationview.setPaymentId(roomAdded.getPaymentId());
            // view 레파지 토리에 save
            reservationviewRepository.save(reservationview);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_UPDATE_1(@Payload Reserved reserved) {
        try {
            if (!reserved.validate()) return;
                // view 객체 조회
            Optional<Reservationview> reservationviewOptional = reservationviewRepository.findById(reserved.getId());

            if( reservationviewOptional.isPresent()) {
                 Reservationview reservationview = reservationviewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 reservationview.setState(reserved.getState());
                // view 레파지 토리에 save
                 reservationviewRepository.save(reservationview);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCancelReserved_then_UPDATE_2(@Payload CancelReserved cancelReserved) {
        try {
            if (!cancelReserved.validate()) return;
                // view 객체 조회
            Optional<Reservationview> reservationviewOptional = reservationviewRepository.findById(cancelReserved.getId());

            if( reservationviewOptional.isPresent()) {
                 Reservationview reservationview = reservationviewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 reservationview.setState(cancelReserved.getState());
                // view 레파지 토리에 save
                 reservationviewRepository.save(reservationview);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomOffered_then_UPDATE_3(@Payload RoomOffered roomOffered) {
        try {
            if (!roomOffered.validate()) return;
                // view 객체 조회
            Optional<Reservationview> reservationviewOptional = reservationviewRepository.findById(roomOffered.getId());

            if( reservationviewOptional.isPresent()) {
                 Reservationview reservationview = reservationviewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 reservationview.setState(roomOffered.getState());
                // view 레파지 토리에 save
                 reservationviewRepository.save(reservationview);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

