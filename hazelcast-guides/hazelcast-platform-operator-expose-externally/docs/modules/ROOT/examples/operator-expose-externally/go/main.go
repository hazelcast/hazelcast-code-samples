package main

import (
	"context"
	"fmt"
	"math/rand"

	"github.com/hazelcast/hazelcast-go-client"
)

func main() {
	config := hazelcast.Config{}
	cc := &config.Cluster
	cc.Network.SetAddresses("<EXTERNAL-IP>")
	cc.Discovery.UsePublicIP = true
	ctx := context.TODO()
	client, err := hazelcast.StartNewClientWithConfig(ctx, config)
	if err != nil {
		panic(err)
	}
	fmt.Println("Successful connection!")
	fmt.Println("Starting to fill the map with random entries.")
	m, err := client.GetMap(ctx, "map")
	if err != nil {
		panic(err)
	}
	for {
		num := rand.Intn(100_000)
		key := fmt.Sprintf("key-%d", num)
		value := fmt.Sprintf("value-%d", num)
		if _, err = m.Put(ctx, key, value); err != nil {
			fmt.Println("ERR:", err.Error())
		} else {
			if mapSize, err := m.Size(ctx); err != nil {
				fmt.Println("ERR:", err.Error())
			} else {
				fmt.Println("Current map size:", mapSize)
			}
		}
	}
}
