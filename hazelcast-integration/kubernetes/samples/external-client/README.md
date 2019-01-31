## Configure Service Account

#### Create Cluster Role

Create `clusterrole.yaml`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: sample-cluster-role
rules:
- apiGroups:
  - ""
  resources:
  - endpoints
  - pods
  - nodes
  verbs:
  - get
  - list
```

Apply Cluster Role.

```
$ kubectl apply -f clusterrole.yaml
```

#### Create Service Account

Create `serviceaccount.yaml`

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sample-service-account
```

Apply Service Account

```
$ kubectl apply -f serviceaccount.yaml
```

#### Create Cluster Role Binding

Create `clusterrolebinding.yaml`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: sample-role-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: sample-cluster-role
subjects:
- kind: ServiceAccount
  name: sample-service-account
  namespace: default
```

Apply Cluster Role Binding

```
$ kubectl apply -f clusterrolebinding.yaml
```

## Fetch Ca Certificate and Access Token

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
            <namespace>default</namespace>
            <kubernetes-master>https://35.226.5.121</kubernetes-master>
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
Jan 31, 2019 1:34:11 PM com.hazelcast.client.config.XmlClientConfigLocator
INFO: Loading 'hazelcast-client.xml' from classpath.
Jan 31, 2019 1:34:12 PM com.hazelcast.client.HazelcastClient
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] A non-empty group password is configured for the Hazelcast client. Starting with Hazelcast version 3.11, clients with the same group name, but with different group passwords (that do not use authentication) will be accepted to a cluster. The group password configuration will be removed completely in a future release.
Jan 31, 2019 1:34:12 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is STARTING
Jan 31, 2019 1:34:13 PM com.hazelcast.spi.discovery.integration.DiscoveryService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Kubernetes Discovery properties: { service-dns: null, service-dns-timeout: 5, service-name: null, service-port: 0, service-label: null, service-label-value: true, namespace: default, resolve-not-ready-addresses: false, kubernetes-master: https://35.226.5.121}
Jan 31, 2019 1:34:14 PM com.hazelcast.spi.discovery.integration.DiscoveryService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Kubernetes Discovery activated resolver: KubernetesApiEndpointResolver
Jan 31, 2019 1:34:14 PM com.hazelcast.client.spi.ClientInvocationService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Running with 2 response threads
Jan 31, 2019 1:34:14 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is STARTED
Jan 31, 2019 1:34:24 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Trying to connect to [10.16.0.16]:5701 as owner member
Jan 31, 2019 1:34:30 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Exception during initial connection to [10.16.0.16]:5701, exception com.hazelcast.core.HazelcastException: java.net.SocketTimeoutException
Jan 31, 2019 1:34:30 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Trying to connect to [10.16.2.27]:5701 as owner member
Jan 31, 2019 1:34:35 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Exception during initial connection to [10.16.2.27]:5701, exception com.hazelcast.core.HazelcastException: java.net.SocketTimeoutException
Jan 31, 2019 1:34:35 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Unable to get alive cluster connection, try in 3000 ms later, attempt 1 of 2.
Jan 31, 2019 1:34:39 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Trying to connect to [10.16.0.16]:5701 as owner member
Jan 31, 2019 1:34:44 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Exception during initial connection to [10.16.0.16]:5701, exception com.hazelcast.core.HazelcastException: java.net.SocketTimeoutException
Jan 31, 2019 1:34:44 PM com.hazelcast.client.connection.ClientConnectionManager
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] Trying to connect to [10.16.2.27]:5701 as owner member
Jan 31, 2019 1:34:49 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Exception during initial connection to [10.16.2.27]:5701, exception com.hazelcast.core.HazelcastException: java.net.SocketTimeoutException
Jan 31, 2019 1:34:49 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Unable to get alive cluster connection, attempt 2 of 2.
Jan 31, 2019 1:34:49 PM com.hazelcast.client.connection.ClientConnectionManager
WARNING: hz.client_0 [dev] [3.11-SNAPSHOT] Could not connect to cluster, shutting down the client. Unable to connect to any address! The following addresses were tried: [[10.16.2.27]:5701, [10.16.0.16]:5701]
Exception in thread "main" java.lang.IllegalStateException: Unable to connect to any address! The following addresses were tried: [[10.16.2.27]:5701, [10.16.0.16]:5701]
	at com.hazelcast.client.connection.nio.ClusterConnector.connectToClusterInternal(ClusterConnector.java:206)
	at com.hazelcast.client.connection.nio.ClusterConnector.access$400(ClusterConnector.java:56)
	at com.hazelcast.client.connection.nio.ClusterConnector$2.call(ClusterConnector.java:215)
	at com.hazelcast.client.connection.nio.ClusterConnector$2.call(ClusterConnector.java:211)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
	at com.hazelcast.util.executor.HazelcastManagedThread.executeRun(HazelcastManagedThread.java:64)
	at com.hazelcast.util.executor.HazelcastManagedThread.run(HazelcastManagedThread.java:80)
Jan 31, 2019 1:34:49 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is SHUTTING_DOWN
Jan 31, 2019 1:34:49 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.11-SNAPSHOT] HazelcastClient 3.11-SNAPSHOT (20181015 - e6d277b) is SHUTDOWN
```
