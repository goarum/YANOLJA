package yanolja.external;

public class Payment {

    private Long id;
    private Integer paymentId;
    private Integer roomId;
    private Integer price;
    
    public Long getId() {
        return id;
    }
    public Integer getRoomId() {
        return roomId;
    }
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Integer getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }

}
