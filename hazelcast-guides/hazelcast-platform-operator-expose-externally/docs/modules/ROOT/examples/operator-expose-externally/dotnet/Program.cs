using Hazelcast;
using Microsoft.Extensions.Logging;

class csharp_example
{
    static async Task Main(string[] args)
    {
        var options = new HazelcastOptionsBuilder()
            .With(args)
            .With((configuration, options) =>
            {
                options.LoggerFactory.Creator = () => LoggerFactory.Create(loggingBuilder => loggingBuilder.AddConfiguration(configuration.GetSection("logging")).AddConsole());
                options.Networking.Addresses.Add("<EXTERNAL-IP>");
                options.Networking.UsePublicAddresses = true;
            })
            .Build();
        var client = await HazelcastClientFactory.StartNewClientAsync(options);


        Console.WriteLine("Successful connection!");
        Console.WriteLine("Starting to fill the map with random entries.");

        var map = await client.GetMapAsync<string, string>("map");
        var random = new Random();
        while (true)
        {
            var randomKey = random.Next(100_000);
            await map.PutAsync("key-" + randomKey, "value-" + randomKey);
            Console.WriteLine("Current map size: " + await map.GetSizeAsync());
        }
    }
}