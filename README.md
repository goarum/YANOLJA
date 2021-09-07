# YANOLJA
![캡처](https://user-images.githubusercontent.com/86760528/132209815-d3d5fe25-d208-4828-9ce1-25a711b6b33b.PNG)

숙박 예약을 위한 어플리케이션을 단순화 하여 Microservice 를 이용하여 구현. ( 개인과제 )

# 설계
### 기능적 요구사항
1. 숙박업체가 방을 등록한다.
2. 고객이 방을 예약 및 결제한다.
3. 숙박업체에서 방을 제공하면 결제금액이 숙박업체에 지급된다.
4. 나의 예약현황에서 예약현황 및 상태를 조회할 수 있다.
5. 고객이 예약을 취소 할 수 있다.
6. 고객이 예약을 취소하면 결제취소 및 환불 되어야 한다.

### 비기능적 요구사항
1. 고객이 예약시에 결재가 되어야 한다.  → REQ/RES Sync 호출
2. 결재가 과중되면 결재를 잠시 후에 하도록 유도한다 → Circuit breaker
3. 고객이 예약상태를 예약내역조회에서 확인할 수 있어야 한다 → CQRS
   

### Event Storming 결과
![image](https://user-images.githubusercontent.com/86760528/132423973-47109fdb-991c-4322-917a-935c26cac4ab.png)


### 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/86760528/132265875-50b2f6a1-00f1-41fd-8e92-cfaf711f0584.png)

# 구현
## 요구사항 충족여부 검증
1. 숙박업체가 방을 등록한다.

![image](https://user-images.githubusercontent.com/86760528/132423064-c4cb2be5-1fe4-48cf-9703-4f7a617f8d49.png)

2. 고객이 방을 예약 및 결제한다.

![image](https://user-images.githubusercontent.com/86760528/132423162-8555190d-6de4-4864-a66b-61290679be86.png)

3. 숙박업체에서 방을 제공하면 결제금액이 숙박업체에 지급된다.

![image](https://user-images.githubusercontent.com/86760528/132423289-f7e17698-a4e9-4075-8934-bf6ec9f339cf.png)

   결재 log에서 돈이 지급되는것을 확인 가능.

![image](https://user-images.githubusercontent.com/86760528/132423382-012c33bd-ba93-4595-acd3-f09f4047030f.png)

5. 나의 예약현황에서 예약현황 및 상태를 조회할 수 있다.

![image](https://user-images.githubusercontent.com/86760528/132423471-9e20ffae-3617-4603-b9b3-4e2549fcfc21.png)

7. 고객이 예약을 취소 할 수 있다.
   -예약된 결과를 취소

![image](https://user-images.githubusercontent.com/86760528/132423684-52f11f79-2f37-4b1a-aa8e-ef8783f41b31.png)
![image](https://user-images.githubusercontent.com/86760528/132423715-cc3eba72-63af-4fe9-8556-83c779954b51.png)

9. 고객이 예약을 취소하면 결제취소 및 환불 되어야 한다.

![image](https://user-images.githubusercontent.com/86760528/132423761-52be2856-3144-4df9-b005-64295ce3acbf.png)

## CheckPoint1. Saga
이벤트 Pub / Sub 구현
* Publish
  room -> room.java / RoomAdded Event Publish 구현부
  
![image](https://user-images.githubusercontent.com/86760528/132422456-173da76b-4530-4cb1-bb61-dd72d9d1c347.png)

* Subscribe
  reservation -> PolicyHandler.java / RoomAdded Event Subscribe 구현부

![image](https://user-images.githubusercontent.com/86760528/132422608-3db2e489-f60a-4d59-b2a6-d139e8555340.png)

## CheckPoint2. CQRS
CQRS 패턴에 따라 Command 와 Query 를 분리하여, viewer를 통해 방등록건(room)의 조회가 가능하다.
viewer의 Content Id는 등록건의 Id 를 그대로 사용하도록 하여 등록건 별로 조회가 가능하도록 하였다.

* 방등록시 viewer 항목 조회 결과 (state : empty)

![image](https://user-images.githubusercontent.com/86760528/132381364-3a8979d1-dcb5-4ddf-bdc7-12f9801770be.png)

* 예약시 viewer의 항목 조회 결과 (state : Reserved)

![image](https://user-images.githubusercontent.com/86760528/132381407-3c0c2b5d-c013-4b3d-83d9-0608677ba216.png)


## CheckPoint3. Correlation
데이터의 흐름이 방예약을 중심으로 이루어 지므로, roomId를 Correlation Key 로 사용하여 처리건을 식별하였다.
결제 서비스 :: 구매요청 완료 후 결제 금액을 숙박업소에게 전송하는 부분. roomId로 결제건을 식별하여 처리한다.

![image](https://user-images.githubusercontent.com/86760528/132377544-3076db23-fef3-4b7e-aa93-7191cfea7ab8.png)

## CheckPoint4. Req/Resp
비기능적 요구사항 [고객이 예약시에 결재가 되어야 한다.] 를 만족시키기 위해
예약 --> 결제 처리 간의 처리방식을 Req/Resp 로 구현하였으며, RestRepository를 이용하였다.

* reservation -> payment 신규 생성 Req/Resp 방식 호출부

![image](https://user-images.githubusercontent.com/86760528/132376430-315c6468-ba2e-4cc8-8deb-d6c2bd72db7e.png)

* reservation -> PaymentService.java / RESTful 함수 FeignClient 정의부

![image](https://user-images.githubusercontent.com/86760528/132376608-4b086ca2-994c-4024-a3d1-5034705a42c0.png)

## CheckPoint5. Gateway
API Gateway를 적용하여, MicroService의 진입점을 단일화 하였다.
* Default Profile : 8080 Port, http://URL:8080/{context}
* Docker Profile : 8080 Port, http://URL:8080/{context}
```
server:
  port: 8080

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: room
          uri: http://localhost:8081
          predicates:
            - Path=/rooms/** 
        - id: reservation
          uri: http://localhost:8082
          predicates:
            - Path=/reservations/** 
        - id: pay
          uri: http://localhost:8083
          predicates:
            - Path=/payments/** 
        - id: Viewer
          uri: http://localhost:8084
          predicates:
            - Path= /reservationviews/**
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


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: room
          uri: http://room:8080
          predicates:
            - Path=/rooms/** 
        - id: reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservations/** 
        - id: pay
          uri: http://pay:8080
          predicates:
            - Path=/payments/** 
        - id: Viewer
          uri: http://Viewer:8080
          predicates:
            - Path= /reservationviews/**
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

server:
  port: 8080
```

## CheckPoint6. Polyglot
타 서비스와 별개로 Viewer 서비스는 SQLDB를 사용하였다. 그 외 서비스는 (h2 사용)

* Viewer 의 application.yml

![image](https://user-images.githubusercontent.com/86760528/132373895-c64f4a03-e351-4d97-b184-4baed74ad850.png)

* viewer의 pom.xml DB 설정 코드

![image](https://user-images.githubusercontent.com/86760528/132375397-6cbc6e52-a07b-4303-9e23-c30f9e092f5b.png)

* SQLDB 조회 결과

![image](https://user-images.githubusercontent.com/86760528/132375082-a98df9c1-4b7d-4571-a6a7-3abd2c426d23.png)

![image](https://user-images.githubusercontent.com/86760528/132375130-ce901aae-8387-4b20-8ff2-bbf081dd80e1.png)


# 운영

## CheckPoint7. Deploy/ Pipeline
* git에서 소스 가져오기
```
git clone https://github.com/goarum/YANOLJA.git
```

* Maven Packaging / Docker Build, Push
```
cd /gateway
mvn package         # Maven Packaging
docker build -t user0303.azurecr.io/gateway:latest .     # Docker Build
docker push user0303.azurecr.io/gateway:latest           # Docker Push to Azure Container Registry

cd ../pay
mvn package         # Maven Packaging
docker build -t user0303.azurecr.io/pay:latest .     # Docker Build
docker push user0303.azurecr.io/pay:latest           # Docker Push to Azure Container Registry

cd ../reservation
mvn package         # Maven Packaging
docker build -t user0303.azurecr.io/reservation:latest .     # Docker Build
docker push user0303.azurecr.io/reservation:latest           # Docker Push to Azure Container Registry

cd ../room
mvn package         # Maven Packaging
docker build -t user0303.azurecr.io/room:latest .     # Docker Build
docker push user0303.azurecr.io/room:latest           # Docker Push to Azure Container Registry

cd ../Viewer
mvn package         # Maven Packaging
docker build -t user0303.azurecr.io/viewer:latest .     # Docker Build
docker push user0303.azurecr.io/viewer:latest           # Docker Push to Azure Container Registry

```

* Yaml 파일을 이용한 Deployment
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pay
  labels:
    app: pay
spec:
  replicas: 1
  selector:
    matchLabels:
      app: pay
  template:
    metadata:
      labels:
        app: pay
    spec:
      containers:
        - name: pay
          image: user0303.azurecr.io/pay:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```
* Deploy 수행
```
cd /gateway/kubernates
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

cd ../WTB/kubernates
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

cd ../WTS/kubernates
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

cd ../Pay/kubernates
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

cd ../Viewer/kubernates
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```
* Deploy 완료

![image](https://user-images.githubusercontent.com/86760528/132373163-4303b7a1-d7aa-460c-b6d8-abd97be68b0e.png)

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

![circuit2](https://user-images.githubusercontent.com/86760528/132372157-4909c08d-9bfc-41d5-888d-b75e014e72cd.PNG)

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
* viewer에서 별도로 사용하는 SQLDB의 패스워드를 ConfigMap으로 처리

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
