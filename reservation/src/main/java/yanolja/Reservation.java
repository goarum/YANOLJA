package yanolja;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String roomType;
    private String state;
    private Integer price;
    private Integer roomId;
    private Integer paymentId;


    @PostUpdate
    public void onPostUpdate(){


        yanolja.external.Payment payment = new yanolja.external.Payment();
        payment.setPrice(this.price);
        payment.setRoomId(this.roomId);

        try{
            ReservationApplication.applicationContext.getBean(yanolja.external.PaymentService.class)
            .pay(payment);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.state="Pay_Failed";
            System.out.println("**** Payment Service TIMEOUT ****");
        }

        if(state.equals("Reserved")){
            Reserved reserved = new Reserved();
            BeanUtils.copyProperties(this, reserved);
            reserved.publishAfterCommit();
        }
        
        if(state.equals("Canceled")){    
            CancelReserved cancelReserved = new CancelReserved();
            BeanUtils.copyProperties(this, cancelReserved);
            cancelReserved.publishAfterCommit();
        }

    }

    @PostPersist
    public void onPostPersist(){
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }




}