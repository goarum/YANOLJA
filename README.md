# YANOLJA
![캡처](https://user-images.githubusercontent.com/86760528/132209815-d3d5fe25-d208-4828-9ce1-25a711b6b33b.PNG)

숙박 예약을 위한 어플리케이션을 단순화 하여 Microservice 를 이용하여 구현. ( 개인과제 )

# 설계
### 기능적 요구사항
1. 숙박업체가 방을 등록한다.
2. 고객이 방을 예약한다.
3. 고객이 결제한다.
4. 숙박업체에서 방을 제공하면 결제금액이 숙박업체에 지급된다.
5. 나의 예약현황에서 예약현황 및 상태를 조회할 수 있다.
6. 고객이 예약을 취소 할 수 있다.
7. 고객이 예약을 취소하면 결제취소 및 환불 되어야 한다.

### 비기능적 요구사항
1. 고객이 예약시에 결재가 되어야 한다.  → REQ/RES Sync 호출
2. 숙박업체의 방등록은 고객예약시스템과 상광없이 가능해야한다 → 장애격리
3. 결재가 과중되면 결재를 잠시 후에 하도록 유도한다 → Circuit breaker
4. 고객이 예약상태를 예약내역조회에서 확인할 수 있어야 한다 → CQRS
   


### Event Storming 결과
![image](https://user-images.githubusercontent.com/86760528/132283508-4ca438a8-0738-4ea0-be15-f48d22411d53.png)


### 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/86760528/132265875-50b2f6a1-00f1-41fd-8e92-cfaf711f0584.png)

# 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다. (각각의 포트넘버는 8080 ~ 8084이다)
```
cd gateway
mvn spring-boot:run

cd Reservation
mvn spring-boot:run

cd Pay
mvn spring-boot:run

cd Ticket
mvn spring-boot:run

cd MyReservation
mvn spring-boot:run
```

## DDD 의 적용
msaez.io를 통해 구현한 Aggregate 단위로 Entity를 선언 후, 구현을 진행하였다.
Entity Pattern과 Repository Pattern을 적용하기 위해 Spring Data REST의 RestRepository를 적용하였다.

**Reservation 서비스의 Reservation.java**
```java 
package movie;

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
    private String userid;
    private String movie;
    private String theater;
    private String time;
    private String seatNo;
    private Integer price;
    private String cardNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.setStatus("Reserved");  // 예약상태 입력 by khos
        reserved.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        movie.external.Pay pay = new movie.external.Pay();
        // mappings goes here
        BeanUtils.copyProperties(this, pay); // Pay 값 설정 by khos
        pay.setReservationId(reserved.getId());
        pay.setStatus("reserved"); // Pay 값 설정 by khos
        ReservationApplication.applicationContext.getBean(movie.external.PayService.class)
            .pay(pay);

    }
    @PreRemove
    public void onPreRemove(){
        CanceledReservation canceledReservation = new CanceledReservation();
        BeanUtils.copyProperties(this, canceledReservation);
        canceledReservation.setStatus("Canceled Reservation");  // 예약상태 입력 by khos
        canceledReservation.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }
    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

```

**Pay 서비스의 Pay.java**
```java
package movie;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long reservationId;
    private String userid;
    private String movie;
    private String theater;
    private String time;
    private Integer price;
    private String cardNo;
    private String status;
    private String seatNo;

    @PostPersist
    public void onPostPersist(){
        Payed payed = new Payed();
        BeanUtils.copyProperties(this, payed);
        payed.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        Payed payed = new Payed();
        BeanUtils.copyProperties(this, payed);
        payed.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){
        CanceledPay canceledPay = new CanceledPay();
        BeanUtils.copyProperties(this, canceledPay);
        canceledPay.setStatus("Canceled Payment");  // 상태 변경 by khos
        canceledPay.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }
    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }


}

```

**Ticket 서비스의 Ticket.java**
```java
package movie;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Ticket_table")
public class Ticket {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long reservationId;
    private Long payId;
    private String userid;
    private String movie;
    private String theater;
    private String time;
    private String seatNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Ticketed ticketed = new Ticketed();
        BeanUtils.copyProperties(this, ticketed);
        ticketed.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        Ticketed ticketed = new Ticketed();
        BeanUtils.copyProperties(this, ticketed);
        ticketed.publishAfterCommit();

    }

    @PreRemove
    public void onPreRemove(){
        CanceledTicket canceledTicket = new CanceledTicket();
        BeanUtils.copyProperties(this, canceledTicket);
        canceledTicket.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }
    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

```

DDD 적용 후 REST API의 테스트를 통하여 정상적으로 동작하는 것을 확인할 수 있었다.

- Resevation 서비스 호출 결과 

![image](https://user-images.githubusercontent.com/86760622/130421675-11836da1-dbe8-48b5-a241-90a1855b7a96.png)

- Pay 서비스 호출 결과 

![image](https://user-images.githubusercontent.com/86760622/130421919-df745446-0c4d-42f6-9792-fcb399062966.png)

- Ticket 서비스 호출 결과

![image](https://user-images.githubusercontent.com/86760622/130422013-a3e30485-5869-4716-84fe-a3a3b49c3277.png)

- MyReservation 서비스 호출 결과 

![image](https://user-images.githubusercontent.com/86760622/130422106-b95d5fcf-92c8-438e-abdd-27250e32464c.png)




# GateWay 적용
API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 GateWay를 적용하였다.

```yaml
server:
  port: 8080

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: Reservation
          uri: http://localhost:8081
          predicates:
            - Path=/reservations/** 
        - id: Pay
          uri: http://localhost:8082
          predicates:
            - Path=/pays/** 
        - id: Ticket
          uri: http://localhost:8083
          predicates:
            - Path=/tickets/** 
        - id: MyReservation
          uri: http://localhost:8084
          predicates:
            - Path= /myReservations/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
```
8080 port로 Reservation 서비스 정상 호출

![image](https://user-images.githubusercontent.com/86760622/130422248-3f5dc3f6-7073-4b18-8ae5-50429dd94ab2.png)



# CQRS/saga/correlation
Materialized View를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이)도 내 서비스의 화면 구성과 잦은 조회가 가능하게 구현해 두었다. 본 프로젝트에서 View 역할은 MyPages 서비스가 수행한다.

예약 실행 후 MyReservation 화면

![image](https://user-images.githubusercontent.com/86760622/130897427-0daeaa06-3e32-40c1-86fa-f8f5bc304ad8.png)

결제 후 MyReservation 화면

![image](https://user-images.githubusercontent.com/86760622/130897551-f2634bd8-4123-411a-9965-b522a4a13964.png)

티켓팅 후 MyReservation 화면

![image](https://user-images.githubusercontent.com/86760622/130897619-0c864297-00c6-48f8-aa0f-4050946db82c.png)

예약취소 후 MyReservation 화면

![image](https://user-images.githubusercontent.com/86760622/130897740-f379f06e-3906-423c-bdb7-21fdb80acceb.png)


위와 같이 주문을 하게되면 Order > Pay > Delivery > MyPage로 주문이 Assigned 되고

주문 취소가 되면 Status가 deliveryCancelled로 Update 되는 것을 볼 수 있다.

또한 Correlation을 Key를 활용하여 Id를 Key값을 하고 원하는 주문하고 서비스간의 공유가 이루어 졌다.

위 결과로 서로 다른 마이크로 서비스 간에 트랜잭션이 묶여 있음을 알 수 있다.

# 폴리글랏
Order 서비스의 DB와 MyPage의 DB를 다른 DB를 사용하여 폴리글랏을 만족시키고 있다.

```
spring:
  profiles: docker
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://user03.database.windows.net:1433;database=yanolja;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
    username: rhdkfma77@user03
    password: srhkjrm7614!
  jpa:
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        # dialect: org.hibernate.dialect.MySQL57Dialect
    hibernate:
      ddl-auto: update
      generate-ddl: true  
```

**Order의 pom.xml DB 설정 코드**

![증빙5](https://github.com/bigot93/forthcafe/blob/main/images/db_conf1.png)

**MyPage의 pom.xml DB 설정 코드**

![증빙6](https://github.com/bigot93/forthcafe/blob/main/images/db_conf2.png)

# 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 결재(Pay)와 배송(Delivery) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 Rest Repository에 의해 노출되어있는 REST 서비스를 FeignClient를 이용하여 호출하도록 한다.

**Pay 서비스 내 external.DeliveryService**
```java
package forthcafe.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="Delivery", url="${api.url.delivery}") 
public interface DeliveryService {

    @RequestMapping(method = RequestMethod.POST, path = "/deliveries", consumes = "application/json")
    public void delivery(@RequestBody Delivery delivery);

}
```

**동작 확인**

잠시 Delivery 서비스 중지
![증빙7](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%941.png)

주문 취소 요청시 Pay 서비스 변화 없음
![증빙8](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%942.png)

Delivery 서비스 재기동 후 주문취소
![증빙9](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%943.png)

Pay 서비스 상태를 보면 2번 주문 정상 취소 처리됨
![증빙9](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%944.png)

Fallback 설정
![image](https://user-images.githubusercontent.com/5147735/109755775-f9b7ae80-7c29-11eb-8add-bdb295dc94e1.png)
![image](https://user-images.githubusercontent.com/5147735/109755797-04724380-7c2a-11eb-8fcd-1c5135000ee5.png)


Fallback 결과(Pay service 종료 후 Order 추가 시)
![image](https://user-images.githubusercontent.com/5147735/109755716-dab91c80-7c29-11eb-9099-ba585115a2a6.png)

# 운영

## CI/CD
* 카프카 설치
```
- 헬름 설치
참고 : http://msaschool.io/operation/implementation/implementation-seven/
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

- Azure Only
kubectl patch storageclass managed -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'

- 카프카 설치
kubectl --namespace kube-system create sa tiller      # helm 의 설치관리자를 위한 시스템 사용자 생성
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update
kubectl create ns kafka
helm install my-kafka --namespace kafka incubator/kafka

kubectl get po -n kafka -o wide
```
* Topic 생성
```
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic forthcafe --create --partitions 1 --replication-factor 1
```
* Topic 확인
```
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list
```
* 이벤트 발행하기
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-producer --broker-list my-kafka:9092 --topic forthcafe
```
* 이벤트 수신하기
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic forthcafe --from-beginning
```

* 소스 가져오기
```
git clone https://github.com/bigot93/forthcafe.git
```

## Deploy / Pipeline

* build 하기
```
cd /forthcafe

cd Order
mvn package 

cd ..
cd Pay
mvn package

cd ..
cd Delivery
mvn package

cd ..
cd gateway
mvn package

cd ..
cd MyPage
mvn package
```

* Azure 레지스트리에 도커 이미지 push, deploy, 서비스생성(방법1 : yml파일 이용한 deploy)
```
cd .. 
cd Order
az acr build --registry skteam01 --image skteam01.azurecr.io/order:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy order --type=ClusterIP --port=8080

cd .. 
cd Pay
az acr build --registry skteam01 --image skteam01.azurecr.io/pay:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy pay --type=ClusterIP --port=8080

cd .. 
cd Delivery
az acr build --registry skteam01 --image skteam01.azurecr.io/delivery:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy delivery --type=ClusterIP --port=8080


cd .. 
cd MyPage
az acr build --registry skteam01 --image skteam01.azurecr.io/mypage:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy mypage --type=ClusterIP --port=8080

cd .. 
cd gateway
az acr build --registry skteam01 --image skteam01.azurecr.io/gateway:v1 .
kubectl create deploy gateway --image=skteam01.azurecr.io/gateway:v1
kubectl expose deploy gateway --type=LoadBalancer --port=8080
```


* Azure 레지스트리에 도커 이미지 push, deploy, 서비스생성(방법2)
```
cd ..
cd Order
az acr build --registry skteam01 --image skteam01.azurecr.io/order:v1 .
kubectl create deploy order --image=skteam01.azurecr.io/order:v1
kubectl expose deploy order --type=ClusterIP --port=8080

cd .. 
cd Pay
az acr build --registry skteam01 --image skteam01.azurecr.io/pay:v1 .
kubectl create deploy pay --image=skteam01.azurecr.io/pay:v1
kubectl expose deploy pay --type=ClusterIP --port=8080


cd .. 
cd Delivery
az acr build --registry skteam01 --image skteam01.azurecr.io/delivery:v1 .
kubectl create deploy delivery --image=skteam01.azurecr.io/delivery:v1
kubectl expose deploy delivery --type=ClusterIP --port=8080


cd .. 
cd gateway
az acr build --registry skteam01 --image skteam01.azurecr.io/gateway:v1 .
kubectl create deploy gateway --image=skteam01.azurecr.io/gateway:v1
kubectl expose deploy gateway --type=LoadBalancer --port=8080

cd .. 
cd MyPage
az acr build --registry skteam01 --image skteam01.azurecr.io/mypage:v1 .
kubectl create deploy mypage --image=skteam01.azurecr.io/mypage:v1
kubectl expose deploy mypage --type=ClusterIP --port=8080

kubectl logs {pod명}
```
* Service, Pod, Deploy 상태 확인
![image](https://user-images.githubusercontent.com/5147735/109769165-2de89a80-7c3d-11eb-8472-2281468fb771.png)


* deployment.yml  참고
```
1. image 설정
2. env 설정 (config Map) 
3. readiness 설정 (무정지 배포)
4. liveness 설정 (self-healing)
5. resource 설정 (autoscaling)
```

![image](https://user-images.githubusercontent.com/5147735/109643506-a8f77580-7b97-11eb-926b-e6c922aa2d1b.png)

## CheckPoint8. Circuit Breaker
* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
* reservation -> Pay 와의 Req/Res 연결에서 요청이 과도한 경우 CirCuit Breaker 통한 격리
* Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 4000 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
* 테스트를 수행하기 위해 아래와 같이 roomId 조건을 만족 할 때에 시간지연이 되도록 하였다.

* Application.yml 설정

![image](https://user-images.githubusercontent.com/86760528/132371447-2428a393-4160-4001-8bfe-57df390febb0.png)

* Pay Microservice 내에 시간지연 설정부분

![image](https://user-images.githubusercontent.com/86760528/132371559-371a7aa7-c813-4719-8076-29c13f52e318.png)

* 구매요청 시 Pay_Failed 발생 화면
  
roomId==3 --> Pay_Failed
![circuit](https://user-images.githubusercontent.com/86760528/132371869-af68928c-b9a1-48ac-9c9c-b3765a2e2708.PNG)

roomId==4 --> Reserved
![circuit](https://user-images.githubusercontent.com/86760528/132371898-9e7c3cfb-359e-4e3e-82af-7af1ecaa5ab0.PNG)


## CheckPoint9. Autoscale (HPA)
예약요청이 다수 발생할 경우 Autoscale을 이용하여 Pod Replica를 3개까지 확장하도록 하였다.
테스트를 위하여 pod 확장의 조건은 부하 50%로 지정하였다.

* hpa.yml
```
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: reservation-hpa
spec:
  maxReplicas: 3
  minReplicas: 1
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: reservation
  targetCPUUtilizationPercentage: 50
```
* hpa 정상 생성 확인

![image](https://user-images.githubusercontent.com/86760528/132370248-a23f870f-bdeb-4d4f-bd92-72010527cce3.png)

* siege 부하 테스트 command
```
siege -c100 -t60S -r10 --content-type "application/json" 'http://reservation:8080/reservations POST {"roomId":"1","price":"3000"}'
```
* siege 부하 테스트 결과 및 부하 테스트 중 pod 확장 모니터링

![hpa](https://user-images.githubusercontent.com/86760528/132370438-b5fa38e4-30e5-4a08-8987-9959383e1cc3.PNG)


* siege 부하 테스트 후 pod 상태조회

![hpa2](https://user-images.githubusercontent.com/86760528/132370620-d6d9b90b-cd19-4385-b7c2-1ec7a52e01d0.PNG)

## CheckPoint10. Zero-downtime deploy (Readiness Probe)
reservation Microservice 내에 Readiness Probe를 설정, siege를 이용하여 부하를 준 후 image 버전 교체를 수행하여
Availability를 확인한다.

* 이미지 변경 전 버전 확인

![readness1](https://user-images.githubusercontent.com/86760528/132368587-69076c17-ddc5-46ab-a74e-1d836a64f528.PNG)

* 무정지 재배포를 위한 Readiness Probe 설정

![image](https://user-images.githubusercontent.com/86760528/132368740-0e796f48-06df-4dcf-b0e9-4a7a2c3b072f.png)

* siege 를 이용하여 -C1의 약한 부하를 가함
```
siege -c1 -t180S -r100 --content-type "application/json" 'http://reservation:8080/reservations POST {"roomId":"1","price":"3000"}'
```
* image 버전을 변경
```
kubectl set image deployment reservation reservation=user0303.azurecr.io/reservation:v2
```
* deploy 모니터링 수행
```
kubectl get deploy -l app=reservation -w
```
![readness2](https://user-images.githubusercontent.com/86760528/132369035-41d48ae7-aead-4610-91bf-1bb59844cb23.PNG)

* siege 부하 결과 확인 (100% Availability)

![readness_seige](https://user-images.githubusercontent.com/86760528/132369085-51d04e45-06ca-4c42-95a5-4ba960a9b48c.PNG)

* 이미지 정상 변경 확인 (describe pod)

![readness3](https://user-images.githubusercontent.com/86760528/132369126-e2a48e1b-be7a-4168-ab6b-5283e4a8cb9c.PNG)

## CheckPoint11. ConfigMap/Persistence Volume
* 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리
* viewer에서 별도로 사용하는 MYSQL의 패스워드를 ConfigMap으로 처리

* application.yml 파일에서 패스워드를 ConfigMap과 연결

![configmap1](https://user-images.githubusercontent.com/86760528/132366832-03b613da-2176-4d9d-bcb1-9387671cbdf6.PNG)

* ConfigMap 생성 및 확인
```
kubectl create configmap dbpass --from-literal=password=********
kubectl get cm dbpass -o yaml
```
![image](https://user-images.githubusercontent.com/86760528/132367461-64b1c7f1-d065-4241-a408-76100ee64f64.png)

* Deployment.yml 에 ConfigMap 적용

![configmap2](https://user-images.githubusercontent.com/86760528/132367636-08518a93-dbd6-4b80-be21-c41a81c6b785.PNG)

## CheckPoint12. Self-healing (Liveness Probe)
* Self-healing 확인을 위한 Liveness Probe 옵션 변경 (Port 변경)

![liveness1](https://user-images.githubusercontent.com/86760528/132368054-68fcdfa2-9115-4aed-9d50-330cf527e1f1.PNG)

* 재배포(Deploy) 후 Pod Restart 확인

![liveness2](https://user-images.githubusercontent.com/86760528/132368120-3e8f0623-6ae6-45b7-abad-d9a2484dea4d.PNG)

* Restart 원인 Liveness Probe 확인

![liveness3](https://user-images.githubusercontent.com/86760528/132368158-34a2dbe5-e822-42bb-b6e6-424dc12f347e.PNG)
