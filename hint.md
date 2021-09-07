
## Practice


http POST http://localhost:8088/wts name="Item001"   
http POST http://localhost:8088/wtbs productId=1 price=3000   
http POST http://localhost:8088/wtbs productId=1 price=2500   
http GET http://localhost:8088/wtbInboxes   
http PATCH http://localhost:8088/wtbInboxes/1 state="Accepted"   
http PATCH http://localhost:8088/wtbInboxes/2 state="Rejected"   
http GET http://localhost:8088/wtbs/1   
http GET http://localhost:8088/wtbs/2   
http GET http://localhost:8088/myPages   
http PATCH http://localhost:8088/wtbInboxes/1 state="Finished"   

20.194.106.39

http POST http://20.194.106.39:8080/wts name="Item001"   
http POST http://20.194.106.39:8080/wtbs productId=1 price=3000   
http POST http://20.194.106.39:8080/wtbs productId=1 price=2500   
http GET http://20.194.106.39:8080/wtbInboxes   
http PATCH http://20.194.106.39:8080/wtbInboxes/1 state="Accepted"   
http PATCH http://20.194.106.39:8080/wtbInboxes/2 state="Rejected"   
http GET http://20.194.106.39:8080/wtbs/1   
http GET http://20.194.106.39:8080/wtbs/2   
http GET http://20.194.106.39:8080/myPages   
http PATCH http://20.194.106.39:8080/wtbInboxes/1 state="Finished"   


#### 0. Azure Setting.

1. 무료체험 시작
2. 리소스 그룹 만들기  
3. kubernates Service (AKS) 만들기  
4. Repository Container

> 리소스 그룹 : skcchhson-rsrcgrp  
kubernates Cluster :skcchhson-aks  
Container Registry : skcchhson


#### 1. Start

1) azure login
```
# Login to Personal
az login --tenant 8c406142-9629-4e34-8232-42000a289c48

# login to Team Prj.
az login --tenant a781af68-05e4-446a-92a7-d9a45d90d58c
```


2) connect azure kubernates service with local kubernates
```
# Personal
az aks get-credentials --resource-group skcchhson-rsrcgrp --name skcchhson-aks

# Team prj.
az aks get-credentials --resource-group skccteam2 --name skccteam2-aks
```

3) Check  
```
kubectl config current-context
```

#### 2.aks -- container Registry Binding

```
az aks update -n skcchhson-aks -g skcchhson-rsrcgrp --attach-acr skcchhson

az aks update -n skccteam2-aks -g skccteam2 --attach-acr skccteam2acr
```

#### 3. Connect Docker CLI -- ACR(Azure Container Registry)

```
# Login to Personal
az acr login --name skcchhson

# Team prj.
az acr login --name skccteam2acr
```

#### 4. Docker Build and Push to Azure Container Repository

```
# .jar 파일 생성
mvn package

# azure container repository 에 등록
docker build -t skcchhson.azurecr.io/wtb:latest .
docker build -t skcchhson.azurecr.io/gateway:latest .

# push docker image to azure container Repository  
docker push skcchhson.azurecr.io/order:latest
docker push skcchhson.azurecr.io/gateway:latest
```

#### 5. kubernates 에서 실행

```
# 실행
kubectl create deployment order --image=skcchhson.azurecr.io/order:latest

kubectl create deployment gateway --image=skccteam2acr.azurecr.io/gateway:latest

# expose (gateway 외)
kubectl expose deploy order --type="ClusterIP" --port:8080

# expose (gateway only)
kubectl expose deploy gateway --type="LoadBalancer" --port 8080

```

kubectl get all 로 External ID 까지 잘 나오는 것 확인 필요.


#### 6. 단일 Microservice Test

```
# Local CMD
http GET http://{ExternalIP}:8080/orders

```
**결과값**  
![](6_단일MSTest1.png)


#### 7. Kafka 설치

MSASchool 의 설치 예제 따라 수행 **(in Azure Console)**  
참고 url : http://msaschool.io/operation/implementation/implementation-seven/

```
# 설치 확인 및 버전 체크
helm version
> version.BuildInfo{Version:"v3.4.0", GitCommit:"7090a89efc8a...
```

helm 3.x 기준 설치방법
```
kubectl --namespace kube-system create sa tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update
kubectl create ns kafka
helm install my-kafka --namespace kafka incubator/kafka
```

-Azure Console 사용 종료 후 VS Code Cmd 로 이동-

```
# Kafka 실행 확인
kubectl get po -n kafka
```
Kafka 3개 / Zookeeper 3개 모두 Running 될때까지 대기  
![](7_Kafka동작확인.png)  

#### 8. Kafka Topic 등록

Topic을 등록 / 확인 --> onlinebookstore
```
# Topic 등록
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic onlinebookstore --create --partitions 1 --replication-factor 1

# 등록된 Topic 확인
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list
```

모니터링 시작
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic onlinebookstore --from-beginning
```

#### 9. Event 발행 Test

1) 신규 도서 등록 후 결과 확인  

![](newbook.png)  

Kafka 이벤트 모니터링 결과  

![](newbook_kafka.png)  


2) 신규 주문 발행 후 결과 확인  

![](ordered.png)

Kafka 이벤트 모니터링 결과  

![](ordered_kafka.png)


#### 10. pcv 설정

**결과값**  
![](6_단일MSTest1.png)








#### 별첨. WSL 적용 안될때

Windows 기능 추가 >> Hyper-V / Virtual Machin / Windows Subsystem Linux(WSL) 체크  
작업관리자 --> 리소스 --> CPU 에 가상화 지원 꺼져있는지 확인  
--> 꺼져있다면, BIOS 들어가서 Advanced >> CPU 에서 Intel 가상화 지원 Enabled


### 이미지 업데이트

kubectl set image deployment.apps/order order=skccteam2acr.azurecr.io/order:v2

skccteam2
skcc@123



![te](assets/markdown-img-paste-20210613233423361.png)
