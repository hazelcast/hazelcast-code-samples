import os

import hazelcast
import hazelcast.discovery

# These environment variables are required
VIRIDIAN_SECRETS_DIR_PROP = "VIRIDIAN_SECRETS_DIR"
VIRIDIAN_CLUSTER_ID_PROP = "VIRIDIAN_CLUSTER_ID"
VIRIDIAN_PASSWORD_PROP = "VIRIDIAN_PASSWORD"
VIRIDIAN_DISCOVERY_TOKEN_PROP = "VIRIDIAN_DISCOVERY_TOKEN"


def viridian_config_present() -> bool:
    return VIRIDIAN_SECRETS_DIR_PROP in os.environ


# kwargs will be passed to the HazelcastClient
def configure_from_environment(**addl_hz_client_args):
    cluster_id = os.environ[VIRIDIAN_CLUSTER_ID_PROP]
    discovery_token = os.environ[VIRIDIAN_DISCOVERY_TOKEN_PROP]
    password = os.environ[VIRIDIAN_PASSWORD_PROP]
    secrets_dir = os.environ[VIRIDIAN_SECRETS_DIR_PROP]
    return configure(cluster_id=cluster_id,
                     discovery_token=discovery_token,
                     password=password,
                     secrets_dir=secrets_dir,
                     **addl_hz_client_args)


def configure(cluster_id: str,
              discovery_token: str,
              password: str,
              secrets_dir: str,
              **addl_hz_client_args) -> hazelcast.HazelcastClient:
    hazelcast.discovery.HazelcastCloudDiscovery._CLOUD_URL_BASE = "api.viridian.hazelcast.com"
    ca_file = os.path.abspath(os.path.join(secrets_dir, "ca.pem"))
    cert_file = os.path.abspath(os.path.join(secrets_dir, "cert.pem"))
    key_file = os.path.abspath(os.path.join(secrets_dir, "key.pem"))
    return hazelcast.HazelcastClient(
        cluster_name=cluster_id,
        cloud_discovery_token=discovery_token,
        statistics_enabled=True,
        ssl_enabled=True,
        ssl_cafile=ca_file,
        ssl_certfile=cert_file,
        ssl_keyfile=key_file,
        ssl_password=password,
        **addl_hz_client_args
    )
