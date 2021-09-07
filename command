docker build -t user0303.azurecr.io/gateway:latest .
docker build -t user0303.azurecr.io/pay:latest .
docker build -t user0303.azurecr.io/reservation:latest .
docker build -t user0303.azurecr.io/room:latest .
docker build -t user0303.azurecr.io/viewer:latest .



docker push user0303.azurecr.io/gateway:latest
docker push user0303.azurecr.io/pay:latest
docker push user0303.azurecr.io/reservation:latest
docker push user0303.azurecr.io/room:latest
docker push user0303.azurecr.io/viewer:latest

kubectl apply -f kubernetes

az aks update -n user03-aks -g user03-rsrcgrp --attach-acr user0303

kubectl create deployment gateway --image=user0303.azurecr.io/gateway:latest


# siege pod Generation command (All at once)
kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
EOF