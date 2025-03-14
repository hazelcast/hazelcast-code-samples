package main

import (
	"context"
	"fmt"
	"math/rand"
	"os"

	"github.com/hazelcast/hazelcast-go-client"
)

func main() {
	if len(os.Args) != 2 {
		fmt.Println("You should pass an argument to run: fill or size")
		return
	}
	if !(os.Args[1] == "fill" || os.Args[1] == "size") {
		fmt.Println("Wrong argument, you should pass: fill or size")
		return
	}

	config := hazelcast.Config{}
	cc := &config.Cluster
	cc.Network.SetAddresses("<EXTERNAL-IP>:5701")
	cc.Unisocket = true
	ctx := context.TODO()
	client, err := hazelcast.StartNewClientWithConfig(ctx, config)
	if err != nil {
		panic(err)
	}
	fmt.Println("Successful connection!")
	m, err := client.GetMap(ctx, "persistent-map")
	if err != nil {
		panic(err)
	}
	if os.Args[1] == "fill" {
		fmt.Println("Starting to fill the map with random entries.")
		for {
			num := rand.Intn(100_000)
			key := fmt.Sprintf("key-%d", num)
			value := fmt.Sprintf("value-%d", num)
			if _, err = m.Put(ctx, key, value); err != nil {
				fmt.Println("ERR:", err.Error())
				continue
			}
			mapSize, err := m.Size(ctx)
			if err != nil {
				fmt.Println("ERR:", err.Error())
				continue
			}
			fmt.Println("Current map size:", mapSize)
		}
		return
	}
	mapSize, err := m.Size(ctx)
	if err != nil {
		fmt.Println("ERR:", err.Error())
		return
	}
	fmt.Println("Current map size:", mapSize)

}
