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
            if (args.Length != 2)
            {
                Console.WriteLine("You need to pass two arguments. The first argument must be `fill` or `size`. The second argument must be `mapName`.");
                return;
            }
            if (!(args[0] == "fill" || args[0] == "size"))
            {
                Console.WriteLine("Wrong argument, you should pass: fill or size");
                return;
            }

            var mapName = args[1];
            var options = new HazelcastOptionsBuilder()                
                .With(args)                
                .With((configuration, options) =>
                {
                    options.LoggerFactory.Creator = () => LoggerFactory.Create(loggingBuilder =>
                        loggingBuilder
                            .AddConsole());

                    options.Networking.UsePublicAddresses = true;
                    options.Networking.SmartRouting = false;
                    options.Networking.Addresses.Add("<EXTERNAL-IP>:5701");
                    
                })
                .Build();



            await using var client = await HazelcastClientFactory.StartNewClientAsync(options);
            
            Console.WriteLine("Successful connection!");
            Console.WriteLine("Starting to fill the map with random entries.");

            var map = await client.GetMapAsync<string, string>(mapName);
            var random = new Random();

            if (args[0] == "fill")
            {
                Console.WriteLine("Starting to fill the map with random entries.");
                while (true)
                {
                    var num = random.Next(100_000);
                    var key = $"key-{num}";
                    var value = $"value-{num}";
                    await map.PutAsync(key, value);
                    var mapSize = await map.GetSizeAsync();
                    Console.WriteLine($"Current map size: {mapSize}");
                }
            }
            else
            {
                var mapSize = await map.GetSizeAsync();
                Console.WriteLine($"Current map size: {mapSize}");
                await client.DisposeAsync();
            }
        }
    } 
}
