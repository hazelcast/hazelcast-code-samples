using System;
using System.Threading.Tasks;
using Hazelcast;
using Microsoft.Extensions.Logging;

namespace Client
{
    public class Program
    {
        static async Task Main(string[] args)
        {
            Console.WriteLine("Hazelcast Kubernetes Client");

            Console.WriteLine("Build options...");            
            var options = new HazelcastOptionsBuilder()                
                .With(args)                
                .With((configuration, options) =>
                {
                    // configure logging factory and add the console provider
                    options.LoggerFactory.Creator = () => LoggerFactory.Create(loggingBuilder =>
                        loggingBuilder
                            .AddConfiguration(configuration.GetSection("logging"))
                            .AddConsole());

                    options.Networking.Addresses.Add("<EXTERNAL IP>");
                    options.Networking.UsePublicAddresses = true;
                })
                .WithDefault("Logging:LogLevel:Hazelcast", "Debug")
                .Build();


            await using var client = await HazelcastClientFactory.StartNewClientAsync(options);

            Console.WriteLine("Successful connection!");
            Console.WriteLine("Starting to fill the map with random entries.");

            var map = await client.GetMapAsync<string, string>("myMap");

            var rndm = new Random();

            while (true)
            {
                var randomValue = rndm.Next(100_000);
                var key = $"key-{randomValue}";
                var val = $"value-{randomValue}";

                await map.PutIfAbsentAsync(key, val);
                Console.WriteLine($"PUT: {key} {val}");
                await Task.Delay(1000);
            }

        }
    }
}
