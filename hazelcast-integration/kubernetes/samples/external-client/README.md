# Hazelcast with external Client

This is a complete example presenting how to use Hazelcast cluster deployed on Kubernetes with Hazelcast Smart Client running outside of Kubernetes.

This example assumes you have a running Kubernetes cluster and the `kubectl` tool installed and configured.  

## Configure Hazelcast cluster on Kubernetes

As the first step, you need to start Hazelcast cluster in such a way that each member is exposed with a separate public IP/port. In Kubernetes PODs can be accessed from outside only via services, so the configuration requires creating a separate service (LoadBalancer or NodePort) for each Hazelcast member POD. The simplest way to achieve it is to use [Metacontroller](https://metacontroller.app/) plugin with [Service-Per-Pod](https://github.com/GoogleCloudPlatform/metacontroller/tree/master/examples/service-per-pod) Decorator Controller.

### 1. Install Metacontroller plugin

To install [Metacontroller](https://metacontroller.app/) plugin, it's enough to execute the following commands.

```
# Create metacontroller namespace.
kubectl create namespace metacontroller
# Create metacontroller service account and role/binding.
kubectl apply -f https://raw.githubusercontent.com/GoogleCloudPlatform/metacontroller/master/manifests/metacontroller-rbac.yaml
# Create CRDs for Metacontroller APIs, and the Metacontroller StatefulSet.
kubectl apply -f https://raw.githubusercontent.com/GoogleCloudPlatform/metacontroller/master/manifests/metacontroller.yaml
```

If you have any issues while creating Metacontroller, it may mean that you don't have `ClusterRole` access to your cluster. Please check [this](https://cloud.google.com/kubernetes-engine/docs/how-to/role-based-access-control#defining_permissions_in_a_role) for details.

### 2. Install Service-Per-Pod DecoratorController

To install [Service-Per-Pod](https://github.com/GoogleCloudPlatform/metacontroller/tree/master/examples/service-per-pod) Decorator Controller, you need to execute the following commands from this Code Sample directory.

```
kubectl create configmap service-per-pod-hooks -n metacontroller --from-file=hooks
kubectl apply -f service-per-pod.yaml
```

This Decorator Controller automatically creates a service for each POD marked with the following annotations:

```yaml
annotations:
    service-per-pod-label: "statefulset.kubernetes.io/pod-name"
    service-per-pod-ports: "5701:5701"
``` 

### 3. Configure Service Account

Hazelcast uses Kubernetes API for the member discovery and therefore it requires granting permission to certain resources. To create ServiceAccount with minimal permissions, run the following command.

```
kubectl apply -f rbac.yaml
```

The Service Account 'hazelcast-service-account' was created and you can use it in all further steps.

### 4. Install Hazelcast cluster

To install Hazelcast cluster, you need to include the Service-Per-Pod annotations into your StatefulSet (or Deployment) Hazelcast configuration. Then, deploy Hazelcast cluster into your Kubernetes environment.

```
kubectl apply -f hazelcast-cluster.yaml
``` 

You can check that for each Hazelcast Member POD there was a service created and that Hazelcast members formed a cluster.

```
$ kubectl get all
NAME              READY     STATUS    RESTARTS   AGE
pod/hazelcast-0   1/1       Running   0          2m
pod/hazelcast-1   1/1       Running   0          1m
pod/hazelcast-2   1/1       Running   0          1m

NAME                  TYPE           CLUSTER-IP      EXTERNAL-IP      PORT(S)          AGE
service/hazelcast-0   LoadBalancer   10.19.241.253   35.188.83.111    5701:30597/TCP   2m
service/hazelcast-1   LoadBalancer   10.19.251.243   35.192.168.46    5701:32718/TCP   2m
service/hazelcast-2   LoadBalancer   10.19.254.0     35.193.248.247   5701:30267/TCP   2m
service/kubernetes    ClusterIP      10.19.240.1     <none>           443/TCP          1h

$ kubectl logs pod/hazelcast-2
...
Members {size:3, ver:3} [
        Member [10.16.1.10]:5701 - abab30fe-5a45-484d-bad5-e60c252572ca
        Member [10.16.2.7]:5701 - 9b948e91-0115-470f-850e-d5cbf2e3b0e1
        Member [10.16.0.8]:5701 - e68ce431-4000-467b-92c6-0072b2601d60 this
]
...
```

## Configure Hazelcast Client outside Kubernetes

When we have a working Hazelcast cluster deployed on Kubernetes, we can connect to it with an external Hazelcast Smart Client. You need first to fetch the credentials of the created Service Account and then use them to configure the client.

### 5. Check Kubernetes Master IP

To check the IP address of the Kubernetes Master, use the following command.

```
$ kubectl cluster-info
Kubernetes master is running at https://35.226.182.228
```

### 6. Check Access Token and CA Certificate

First, you need to find the name of the secret for the created Service Account.

```
$ kubectl get secret
NAME                                 TYPE                                  DATA      AGE
default-token-q9sp8                  kubernetes.io/service-account-token   3         2h
hazelcast-service-account-token-6s94h   kubernetes.io/service-account-token   3         9m
```

Then, to fetch Access Token, use the following command.

```
$ kubectl get secret hazelcast-service-account-token-6s94h -o jsonpath={.data.token} | base64 --decode | xargs echo
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNhbXBsZS1zZXJ2aWNlLWFjY291bnQtdG9rZW4tNnM5NGgiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoic2FtcGxlLXNlcnZpY2UtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjI5OTI1NzBmLTI1NDQtMTFlOS1iNjg3LTQyMDEwYTgwMDI4YiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OnNhbXBsZS1zZXJ2aWNlLWFjY291bnQifQ.o-j4e-ducrMmQc23xYDnPr6TIyzlAs3pLNAmGLqPe9Vq1mwsxOh3ujcVKR90HAdkfHIF_Sw66qC9hXIDvxfqN_rLXlOKbvTX3gjDrAnyY_93Y3MpmSBj8yR9yHMb4O29a9UIwN5F2_VoCsc0IGumScU_EhPYc9mvEXlwp2bATQOEU-SVAGYPqvVPs9h5wjWZ7WUQa_-RBLMF6KRc9EP2i3c7dPSRVL9ZQ6k6OyUUOVEaPa1tqIxP7vOgx9Tg2C1KmYF5RDrlzrWkhEcjd4BLTiYDKEyaoBff9RqdPYlPwu0YcEH-F7yU8tTDN74KX5jvah3amg_zTiXeNoe5ZFcVdg
```

To fetch CA Certificate, use the following command.

```
$ kubectl get secret hazelcast-service-account-token-6s94h -o jsonpath={.data.ca\\.crt} | base64 --decode | xargs echo
-----BEGIN CERTIFICATE-----
MIIDCzCCAfOgAwIBAgIQVcTHv3jK6g1l7Ph9Xyd9DTANBgkqhkiG9w0BAQsFADAv
MS0wKwYDVQQDEyQ4YjRhNjgwMS04NzJhLTQ2NDEtYjIwOC0zYjEyNDEwYWVkMTcw
HhcNMTkwMTMxMDcyNDMxWhcNMjQwMTMwMDgyNDMxWjAvMS0wKwYDVQQDEyQ4YjRh
NjgwMS04NzJhLTQ2NDEtYjIwOC0zYjEyNDEwYWVkMTcwggEiMA0GCSqGSIb3DQEB
AQUAA4IBDwAwggEKAoIBAQCaty8l9aHeWE1r9yLWKJMa3YQotVclYoEHegB8y6Ke
+zKqa06JKKrz3Qony97VdWR/NMpRYXouSF0owDv9BIoLTC682wlQtNB1c4pTVW7a
AikoNtyNIT8gtA5w0MyjFrbNslUblXvuo0HIeSmJREUmT7BC3VaKgkg64mVdf0DJ
NyrcL+qyCs1m03mi12hgzI72O3qgEtP91tu/oCUdOh39u13TB0fj5tgWURMFgkxo
T0xiNfPueV3pe8uYxBntzFn/74ibiizLRP6d/hsuRdS7IA+bvRLKG/paYwyZuMFb
BDA+kXXAIkOvCpIQCkAKMpyyDz9lBVCtl3eRSAJQLBefAgMBAAGjIzAhMA4GA1Ud
DwEB/wQEAwICBDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBP
TBRY1IbkFJuboMKLW9tdpIzW7hf2qsTLOhtlaJbMXrWaXCTrl8qUgBUZ1sWAW9Uk
qETwRoCMl1Ht7PhbnXEGDNt3Sw3Y3feR4PsffhcgWH0BK8pZVY0Q1zbZ6dVNbU82
EUrrcnV0uiB/JFsJ3rg8qJurutro3uIzAhb9ixYRqYnXUR4q0bxahO04iSUHvtYQ
JmWp1GCb/ny9MyeTkwh2Q+WIQBHsX4LfrKjPwJd6qZME7BmwryYBTkGa0FinmhRg
SdSPEQKmuXmghPU5GLudiI2ooOaqOXIjVPfM/cw4uU9FCGM49qufccOOt6utk0SM
DwupAKLLiaYs47a8JgUa
-----END CERTIFICATE-----
```

### 7. Configure Hazelcast Client

Modify `src/main/resources/hazelcast-client.xml` to include your credentials.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast-client xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.hazelcast.com/schema/client-config
                               http://www.hazelcast.com/schema/client-config/hazelcast-client-config-3.11.xsd"
                  xmlns="http://www.hazelcast.com/schema/client-config">
    <network>
        <kubernetes enabled="true">
            <use-public-ip>true</use-public-ip>
            <kubernetes-master>https://35.226.182.228</kubernetes-master>
            <api-token>eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNhbXBsZS1zZXJ2aWNlLWFjY291bnQtdG9rZW4tNnM5NGgiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoic2FtcGxlLXNlcnZpY2UtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjI5OTI1NzBmLTI1NDQtMTFlOS1iNjg3LTQyMDEwYTgwMDI4YiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OnNhbXBsZS1zZXJ2aWNlLWFjY291bnQifQ.o-j4e-ducrMmQc23xYDnPr6TIyzlAs3pLNAmGLqPe9Vq1mwsxOh3ujcVKR90HAdkfHIF_Sw66qC9hXIDvxfqN_rLXlOKbvTX3gjDrAnyY_93Y3MpmSBj8yR9yHMb4O29a9UIwN5F2_VoCsc0IGumScU_EhPYc9mvEXlwp2bATQOEU-SVAGYPqvVPs9h5wjWZ7WUQa_-RBLMF6KRc9EP2i3c7dPSRVL9ZQ6k6OyUUOVEaPa1tqIxP7vOgx9Tg2C1KmYF5RDrlzrWkhEcjd4BLTiYDKEyaoBff9RqdPYlPwu0YcEH-F7yU8tTDN74KX5jvah3amg_zTiXeNoe5ZFcVdg</api-token>
            <ca-certificate>
                -----BEGIN CERTIFICATE-----
                MIIDCzCCAfOgAwIBAgIQVcTHv3jK6g1l7Ph9Xyd9DTANBgkqhkiG9w0BAQsFADAv
                MS0wKwYDVQQDEyQ4YjRhNjgwMS04NzJhLTQ2NDEtYjIwOC0zYjEyNDEwYWVkMTcw
                HhcNMTkwMTMxMDcyNDMxWhcNMjQwMTMwMDgyNDMxWjAvMS0wKwYDVQQDEyQ4YjRh
                NjgwMS04NzJhLTQ2NDEtYjIwOC0zYjEyNDEwYWVkMTcwggEiMA0GCSqGSIb3DQEB
                AQUAA4IBDwAwggEKAoIBAQCaty8l9aHeWE1r9yLWKJMa3YQotVclYoEHegB8y6Ke
                +zKqa06JKKrz3Qony97VdWR/NMpRYXouSF0owDv9BIoLTC682wlQtNB1c4pTVW7a
                AikoNtyNIT8gtA5w0MyjFrbNslUblXvuo0HIeSmJREUmT7BC3VaKgkg64mVdf0DJ
                NyrcL+qyCs1m03mi12hgzI72O3qgEtP91tu/oCUdOh39u13TB0fj5tgWURMFgkxo
                T0xiNfPueV3pe8uYxBntzFn/74ibiizLRP6d/hsuRdS7IA+bvRLKG/paYwyZuMFb
                BDA+kXXAIkOvCpIQCkAKMpyyDz9lBVCtl3eRSAJQLBefAgMBAAGjIzAhMA4GA1Ud
                DwEB/wQEAwICBDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBP
                TBRY1IbkFJuboMKLW9tdpIzW7hf2qsTLOhtlaJbMXrWaXCTrl8qUgBUZ1sWAW9Uk
                qETwRoCMl1Ht7PhbnXEGDNt3Sw3Y3feR4PsffhcgWH0BK8pZVY0Q1zbZ6dVNbU82
                EUrrcnV0uiB/JFsJ3rg8qJurutro3uIzAhb9ixYRqYnXUR4q0bxahO04iSUHvtYQ
                JmWp1GCb/ny9MyeTkwh2Q+WIQBHsX4LfrKjPwJd6qZME7BmwryYBTkGa0FinmhRg
                SdSPEQKmuXmghPU5GLudiI2ooOaqOXIjVPfM/cw4uU9FCGM49qufccOOt6utk0SM
                DwupAKLLiaYs47a8JgUa
                -----END CERTIFICATE-----
            </ca-certificate>
        </kubernetes>
    </network>
</hazelcast-client>
```

### 8. Run Hazelcast Client application

You can run the client application with the following command.

```
mvn spring-boot:run
```

Application is a web service that uses Hazelcast Client to connect to the Hazelcast cluster. 

To check it works correctly, you can:
* Open browser at: `http://localhost:8080/put?key=sampleKey&value=sampleValue` (you should see a reply `{"response":null}`)
* Open browser at: `http://localhost:8080/get?key=sampleKey` (you should see a reply `{"response":"sampleValue"}`)
* Check the application logs to find:
```
Members [3] {
        Member [10.16.1.10]:5701 - abab30fe-5a45-484d-bad5-e60c252572ca
        Member [10.16.2.7]:5701 - 9b948e91-0115-470f-850e-d5cbf2e3b0e1
        Member [10.16.0.8]:5701 - e68ce431-4000-467b-92c6-0072b2601d60
}
```