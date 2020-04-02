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
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImhhemVsY2FzdC1zZXJ2aWNlLWFjY291bnQtdG9rZW4tcTdoNDQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiaGF6ZWxjYXN0LXNlcnZpY2UtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjA5NDNmZjVmLTc0MTktMTFlYS1hOGE3LTQyMDEwYTgwMDAyMCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmhhemVsY2FzdC1zZXJ2aWNlLWFjY291bnQifQ.GW6F6oNRjQ0z0jVWoMk-m5ePCbzm1pDGFoxINmAs6-KQaCh69PqQyjVU1uhrvue7ndPBaFqm0vs4OO969oeLt701ZIskyw5mEz8VBV-A53-zvx0TcOAOl_x6XNmk-mjX3pJa6D8WWWdnt_uFPw1L8_4GxDRiidaqFZULtS-50fcJERub7wLQUFlNf7DMR_c0rkZvWBlJqUQ5Wm41Y-EdmfTiv4Dsok0bKJQ9zhe9g2oNIPtEKmeO0No7C9v7Jqf5bxM8HzXzjp5AE0q9PK9XSRzn5CXaQzQspuLDaEwdagc8mDVUPx0jw703aQCFwfJw32r7VnC8URrXaaPatQcXug
```

To fetch CA Certificate, use the following command.

```
$ kubectl get secret hazelcast-service-account-token-6s94h -o jsonpath={.data.ca\\.crt} | base64 --decode
-----BEGIN CERTIFICATE-----
MIIDDDCCAfSgAwIBAgIRAPAL3Bz0Bi8uGTDlb5n/3UIwDQYJKoZIhvcNAQELBQAw
LzEtMCsGA1UEAxMkNTY5NWQxNDgtNjMxNS00MzQxLThiNTctZmQ0ZTU2MWVhY2Ux
MB4XDTIwMDQwMTExMTAxNFoXDTI1MDMzMTEyMTAxNFowLzEtMCsGA1UEAxMkNTY5
NWQxNDgtNjMxNS00MzQxLThiNTctZmQ0ZTU2MWVhY2UxMIIBIjANBgkqhkiG9w0B
AQEFAAOCAQ8AMIIBCgKCAQEA3lsZmqurcoiXE0kGSoDyq/4P5YLx56uSfeRPZgPP
fg8mkjLe2r360xZAqiO0cW6yufruKhRbTHesuJn+vmgLzUSYRMQibS9XFAuch6Qb
y2Zvu+ysU3ixzhQ92NmU/gfU70lXiIcGtEAdmk/G+Y3DxIUHXxtGoBMN/HykXWR+
Am9A3VfVm+uD9w4hquwWJyXHUgwXagU7uY8HNjPXygmWI+VuxtMIsNS8UrR9w4rh
nZenp/JKSyCBdGp718287kgV6aCH2B1lxNrrHDMn4RyX3m0vVD1Gw/k1JlHoNmN2
WZTwovnbQnle6iZkKOakhwRNq4Vgqq97nnm4O3EcdzQMoQIDAQABoyMwITAOBgNV
HQ8BAf8EBAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEA
mMRAUci+N67pjBSoCHkCfmeRC9RoWbbnG9k0ZeKkA+KYoyjH1hZYMmJU2RM5+RB9
JiddivrF32S6uYelrCja433ghjfZav7sqdEhBoQpSrv+bWWDtOWUgqLQ5kIFawT3
ZSvETCROnYCipghp8mMPtRBmXZzIkwYalHkK3Gh6+0JiZfSsvpP3yXA3Ne7hIWZD
2zgWthaLEINvG8rsNKLAs/hoQzXZ2YeJ1lUpLvLgPOJX7h5TAmu0biHjim8I7RyC
tk0ffDT2kI56cxbv9fxhSQRGRq7k83Ro/nCMzJ1jpP8CzGkJZhT9BpoZM9aTnFxu
Jw0q61f9lLHqu3uuqOA5KQ==
-----END CERTIFICATE-----
```

### 7. Configure Hazelcast Client

Modify `src/main/resources/hazelcast-client.yaml` to include your credentials.

```yaml
hazelcast-client:
  network:
    kubernetes:
      enabled: true
      namespace: default
      use-public-ip: true
      kubernetes-master: https://35.226.182.228
      api-token: eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImhhemVsY2FzdC1zZXJ2aWNlLWFjY291bnQtdG9rZW4tcTdoNDQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiaGF6ZWxjYXN0LXNlcnZpY2UtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjA5NDNmZjVmLTc0MTktMTFlYS1hOGE3LTQyMDEwYTgwMDAyMCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmhhemVsY2FzdC1zZXJ2aWNlLWFjY291bnQifQ.GW6F6oNRjQ0z0jVWoMk-m5ePCbzm1pDGFoxINmAs6-KQaCh69PqQyjVU1uhrvue7ndPBaFqm0vs4OO969oeLt701ZIskyw5mEz8VBV-A53-zvx0TcOAOl_x6XNmk-mjX3pJa6D8WWWdnt_uFPw1L8_4GxDRiidaqFZULtS-50fcJERub7wLQUFlNf7DMR_c0rkZvWBlJqUQ5Wm41Y-EdmfTiv4Dsok0bKJQ9zhe9g2oNIPtEKmeO0No7C9v7Jqf5bxM8HzXzjp5AE0q9PK9XSRzn5CXaQzQspuLDaEwdagc8mDVUPx0jw703aQCFwfJw32r7VnC8URrXaaPatQcXug
      ca-certificate: |
        -----BEGIN CERTIFICATE-----
        MIIDDDCCAfSgAwIBAgIRAPAL3Bz0Bi8uGTDlb5n/3UIwDQYJKoZIhvcNAQELBQAw
        LzEtMCsGA1UEAxMkNTY5NWQxNDgtNjMxNS00MzQxLThiNTctZmQ0ZTU2MWVhY2Ux
        MB4XDTIwMDQwMTExMTAxNFoXDTI1MDMzMTEyMTAxNFowLzEtMCsGA1UEAxMkNTY5
        NWQxNDgtNjMxNS00MzQxLThiNTctZmQ0ZTU2MWVhY2UxMIIBIjANBgkqhkiG9w0B
        AQEFAAOCAQ8AMIIBCgKCAQEA3lsZmqurcoiXE0kGSoDyq/4P5YLx56uSfeRPZgPP
        fg8mkjLe2r360xZAqiO0cW6yufruKhRbTHesuJn+vmgLzUSYRMQibS9XFAuch6Qb
        y2Zvu+ysU3ixzhQ92NmU/gfU70lXiIcGtEAdmk/G+Y3DxIUHXxtGoBMN/HykXWR+
        Am9A3VfVm+uD9w4hquwWJyXHUgwXagU7uY8HNjPXygmWI+VuxtMIsNS8UrR9w4rh
        nZenp/JKSyCBdGp718287kgV6aCH2B1lxNrrHDMn4RyX3m0vVD1Gw/k1JlHoNmN2
        WZTwovnbQnle6iZkKOakhwRNq4Vgqq97nnm4O3EcdzQMoQIDAQABoyMwITAOBgNV
        HQ8BAf8EBAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEA
        mMRAUci+N67pjBSoCHkCfmeRC9RoWbbnG9k0ZeKkA+KYoyjH1hZYMmJU2RM5+RB9
        JiddivrF32S6uYelrCja433ghjfZav7sqdEhBoQpSrv+bWWDtOWUgqLQ5kIFawT3
        ZSvETCROnYCipghp8mMPtRBmXZzIkwYalHkK3Gh6+0JiZfSsvpP3yXA3Ne7hIWZD
        2zgWthaLEINvG8rsNKLAs/hoQzXZ2YeJ1lUpLvLgPOJX7h5TAmu0biHjim8I7RyC
        tk0ffDT2kI56cxbv9fxhSQRGRq7k83Ro/nCMzJ1jpP8CzGkJZhT9BpoZM9aTnFxu
        Jw0q61f9lLHqu3uuqOA5KQ==
        -----END CERTIFICATE-----

```

### 8. Run Hazelcast Client application

You can run the client application with the following command.

```
mvn spring-boot:run
```

Application is a web service that uses Hazelcast Client to connect to the Hazelcast cluster. 

To check it works correctly, you can execute the following commands:

```
$ curl localhost:8080/put?key=some-key\&value=some-value
{"response":null}

$ curl localhost:8080/get?key=some-key
{"response":"some-value"}
```

You can also check the application logs to see:
```
Members [3] {
        Member [10.16.1.10]:5701 - abab30fe-5a45-484d-bad5-e60c252572ca
        Member [10.16.2.7]:5701 - 9b948e91-0115-470f-850e-d5cbf2e3b0e1
        Member [10.16.0.8]:5701 - e68ce431-4000-467b-92c6-0072b2601d60
}
```