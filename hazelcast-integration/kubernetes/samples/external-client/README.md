## Configure Hazelcast Server

## Install Metacontroller with Service-Per-Pod DecoratorController

#### Install Metacontroller

```
# Create metacontroller namespace.
kubectl create namespace metacontroller
# Create metacontroller service account and role/binding.
kubectl apply -f https://raw.githubusercontent.com/GoogleCloudPlatform/metacontroller/master/manifests/metacontroller-rbac.yaml
# Create CRDs for Metacontroller APIs, and the Metacontroller StatefulSet.
kubectl apply -f https://raw.githubusercontent.com/GoogleCloudPlatform/metacontroller/master/manifests/metacontroller.yaml
```

If you're interested, you can read more about Metacontroller [here](https://metacontroller.app).

#### Install Service-Per-Pod DecoratorController

```
kubectl create configmap service-per-pod-hooks -n metacontroller --from-file=hooks
kubectl apply -f service-per-pod.yaml
```

To read more about Service-Per-Pod decorator, please check [here](https://github.com/GoogleCloudPlatform/metacontroller/tree/master/examples/service-per-pod).

## Install Hazelcast cluster with Service-Per-Pod Decorator

TBD

## Configure Service Account

#### Create Cluster Role

```
kubectl apply -f clusterrole.yaml
```

#### Create Service Account

```
kubectl apply -f serviceaccount.yaml
```

#### Create Cluster Role Binding

```
kubectl apply -f clusterrolebinding.yaml
```

## Fetch Ca Certificate and Access Token

#### Check Kubernetes Master IP

```
$ kubectl cluster-info
Kubernetes master is running at https://35.226.182.228
```

#### Check secret name for the create service account

```
$ kubectl get secret
NAME                                 TYPE                                  DATA      AGE
default-token-q9sp8                  kubernetes.io/service-account-token   3         2h
sample-service-account-token-6s94h   kubernetes.io/service-account-token   3         9m
```

#### Fetch Access Token

```
$ kubectl get secret sample-service-account-token-6s94h -o jsonpath={.data.token} | base64 -d
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNhbXBsZS1zZXJ2aWNlLWFjY291bnQtdG9rZW4tNnM5NGgiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoic2FtcGxlLXNlcnZpY2UtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjI5OTI1NzBmLTI1NDQtMTFlOS1iNjg3LTQyMDEwYTgwMDI4YiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OnNhbXBsZS1zZXJ2aWNlLWFjY291bnQifQ.o-j4e-ducrMmQc23xYDnPr6TIyzlAs3pLNAmGLqPe9Vq1mwsxOh3ujcVKR90HAdkfHIF_Sw66qC9hXIDvxfqN_rLXlOKbvTX3gjDrAnyY_93Y3MpmSBj8yR9yHMb4O29a9UIwN5F2_VoCsc0IGumScU_EhPYc9mvEXlwp2bATQOEU-SVAGYPqvVPs9h5wjWZ7WUQa_-RBLMF6KRc9EP2i3c7dPSRVL9ZQ6k6OyUUOVEaPa1tqIxP7vOgx9Tg2C1KmYF5RDrlzrWkhEcjd4BLTiYDKEyaoBff9RqdPYlPwu0YcEH-F7yU8tTDN74KX5jvah3amg_zTiXeNoe5ZFcVdg
```

#### Fetch CA Certificate

```
$ kubectl get secret sample-service-account-token-6s94h -o jsonpath={.data.ca\\.crt} | base64 -d
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

## Configure Hazelcast Client

#### Hazelcast Configuration

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

#### Running Hazelcast Client

Logs:
```
Feb 20, 2019 3:01:05 PM com.hazelcast.client.config.XmlClientConfigLocator
INFO: Loading 'hazelcast-client.xml' from classpath.
Feb 20, 2019 3:01:06 PM com.hazelcast.client.HazelcastClient
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] A non-empty group password is configured for the Hazelcast client. Starting with Hazelcast version 3.11, clients with the same group name, but with different group passwords (that do not use authentication) will be accepted to a cluster. The group password configuration will be removed completely in a future release.
Feb 20, 2019 3:01:06 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is STARTING
Feb 20, 2019 3:01:07 PM com.hazelcast.spi.discovery.integration.DiscoveryService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Kubernetes Discovery properties: { service-dns: null, service-dns-timeout: 5, service-name: null, service-port: 0, service-label: null, service-label-value: true, namespace: default, resolve-not-ready-addresses: false, kubernetes-master: https://35.226.182.228}
Feb 20, 2019 3:01:07 PM com.hazelcast.spi.discovery.integration.DiscoveryService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Kubernetes Discovery activated resolver: KubernetesApiEndpointResolver
Feb 20, 2019 3:01:07 PM com.hazelcast.client.spi.ClientInvocationService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Running with 2 response threads
Feb 20, 2019 3:01:07 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is STARTED
Node: 1
Feb 20, 2019 3:01:14 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Trying to connect to [10.16.1.8]:5701 as owner member
Feb 20, 2019 3:01:17 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Setting ClientConnection{alive=true, connectionId=1, channel=NioChannel{/192.168.105.9:55311->/35.188.214.176:5701}, remoteEndpoint=[10.16.1.8]:5701, lastReadTime=2019-02-20 15:01:17.823, lastWriteTime=2019-02-20 15:01:17.671, closedTime=never, connected server version=3.11.1} as owner with principal ClientPrincipal{uuid='e75d0a60-9955-4d80-a5e7-ac8a81629dc3', ownerUuid='d92556f2-a03a-48f9-b3c5-aa3e2e585f6f'}
Feb 20, 2019 3:01:17 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Authenticated with server [10.16.1.8]:5701, server version:3.11.1 Local address: /192.168.105.9:55311
Feb 20, 2019 3:01:18 PM com.hazelcast.client.spi.impl.ClientMembershipListener
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] 

Members [3] {
	Member [10.16.2.10]:5701 - 462589d9-7779-4f60-8c9e-71b50a32620d
	Member [10.16.1.8]:5701 - d92556f2-a03a-48f9-b3c5-aa3e2e585f6f
	Member [10.16.0.15]:5701 - 011893fc-83d4-4b75-abe1-4158c95ac140
}
```
