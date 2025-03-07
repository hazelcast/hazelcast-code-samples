# docker-grafana-graphite makefile

# Environment Variables
CONTAINER = grafana-dashboard

.PHONY: up

clean : 
	rm -f grafana-data/*.db 

up :
	docker-compose --profile=${FLIGHT_TELEMETRY_HZ_INSTANCE_MODE} up -d

down :
	docker-compose --profile=${FLIGHT_TELEMETRY_HZ_INSTANCE_MODE} down

shell :
	docker exec -ti $(CONTAINER) /bin/bash

tail :
	docker logs -f $(CONTAINER)
