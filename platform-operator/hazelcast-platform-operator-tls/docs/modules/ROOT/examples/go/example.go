package main

import (
	"context"
	"crypto/tls"
	"fmt"

	"github.com/hazelcast/hazelcast-go-client"
	"github.com/hazelcast/hazelcast-go-client/cluster"
)

func main() {
	var clusterConfig cluster.Config
	clusterConfig.Network.SetAddresses("<EXTERNAL-IP>")
	clusterConfig.Discovery.UsePublicIP = true
	clusterConfig.Network.SSL.Enabled = true
	clusterConfig.Network.SSL.SetTLSConfig(&tls.Config{InsecureSkipVerify: true})

	ctx := context.Background()
	config := hazelcast.Config{Cluster: clusterConfig}
	client, err := hazelcast.StartNewClientWithConfig(ctx, config)
	if err != nil {
		panic(err)
	}
	fmt.Println("Successful connection!", client)
}
