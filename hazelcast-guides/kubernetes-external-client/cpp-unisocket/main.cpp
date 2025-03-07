#include <hazelcast/client/hazelcast_client.h>

int main() {
    hazelcast::client::client_config config;
    config.get_network_config().add_address(hazelcast::client::address{"<EXTERNAL-IP>", 5701})
                               .set_smart_routing(false);
    auto hz = hazelcast::new_client(std::move(config)).get();
    std::cout << "Successful connection!" << std::endl;
    std::cout << "Starting to fill the map with random entries." << std::endl;
    auto map = hz.get_map("map").get();
    while (true) {
        int random_key = rand() % 100000;
        try {
            map->put("key-" + std::to_string(random_key), "value-" + std::to_string(random_key));
            std::cout << "Current map size: " + std::to_string(map->size().get()) << std::endl;
            std::this_thread::sleep_for(std::chrono::seconds(1));
        } catch (const std::exception& e) {
            std::cout << e.what() << std::endl;
        }
    }

    return 0;
}
