# Java Properties
default['java']['jdk_version']="7"
default['java']['install_flavor']="oracle"
default['java']['accept_license_agreement']="true"
default['java']['oracle']['accept_oracle_download_terms']="true"
default['java']['ark_retries']=3

# Default attribute list for application binaries
default['hazelcast']['current_dir']="/opt/hazelcast"
default['hazelcast']['distribution']="/mnt/hazelcast-integration-amazon-ec2/target"

# Attributes for template
default['hazelcast']['group_username']="dev"
default['hazelcast']['group_password']="dev-pass"

default['hazelcast']['mgmt_center_enabled']="false"

default['hazelcast']['network_outbound_ports']="0"
default['hazelcast']['network_port_auto_increment']="true"
default['hazelcast']['network_port_count']="100"
default['hazelcast']['network_port_start']="5701"

default['hazelcast']['network_multicast_enabled']="true"
default['hazelcast']['network_multicast_group']="224.2.2.3"
default['hazelcast']['network_multicast_port']="54327"

default['hazelcast']['network_tcp_ip_enabled']="false"
default['hazelcast']['network_tcp_ip_interface']="127.0.0.1"

# Hazelcast AWS Defaults
default['hazelcast']['network_aws_enabled']="false"
default['hazelcast']['network_aws_access_key']="my_aws_access_key"
default['hazelcast']['network_aws_secret_key']="my_aws_secret_key"
default['hazelcast']['network_aws_region']="us-west-1"
default['hazelcast']['network_aws_host_header']="ec2.amazonaws.com"
default['hazelcast']['network_aws_security_group']="hazelcast-sg"
default['hazelcast']['network_aws_tag_key']="hazelcast_service"
default['hazelcast']['network_aws_tag_value']="true"
default['hazelcast']['network_aws_elastic_ip']=""
default['hazelcast']['network_aws_connection_timeout_seconds']="120"

default['hazelcast']['network_interfaces_enabled']="false"
default['hazelcast']['network_interfaces_networks']=["10.0.2.*","10.10.1.*"]

default['hazelcast']['network_ssl_enabled']="false"
default['hazelcast']['network_socket_interceptor']="false"
default['hazelcast']['network_ssl_sym_encryption_enabled']="false"
default['hazelcast']['network_ssl_sym_encryption_alg']="PBEWithMD5AndDES"
default['hazelcast']['network_ssl_sym_encryption_salt']="thesalt"
default['hazelcast']['network_ssl_sym_encryption_password']="thepass"
default['hazelcast']['network_ssl_sym_encryption_iteration_count']="19"
default['hazelcast']['network_ssl_asym_encryption_enabled']="false"
default['hazelcast']['network_ssl_asym_encryption_alg']="RSA/NONE/PKCS1PADDING"
default['hazelcast']['network_ssl_asym_encryption_key_password']="thekeypass"
default['hazelcast']['network_ssl_asym_encryption_key_alias']="local"
default['hazelcast']['network_ssl_asym_encryption_store_type']="JKS"
default['hazelcast']['network_ssl_asym_encryption_store_password']="thestorepass"
default['hazelcast']['network_ssl_asym_encryption_store_path']="keystore"

default['hazelcast']['partition_group_enabled']="false"

default['hazelcast']['executor_core_pool_size']="16"
default['hazelcast']['executor_max_pool_size']="64"
default['hazelcast']['executor_keep_alive_seconds']="60"

default['hazelcast']['queue_name']="default"
default['hazelcast']['queue_max_size_per_jvm']="0"
default['hazelcast']['queue_backing_map_ref']="default"

default['hazelcast']['map_name']="default"
default['hazelcast']['map_backup_count']="1"
default['hazelcast']['map_async_backup_count']="0"
default['hazelcast']['map_ttl_seconds']="0"
default['hazelcast']['map_max_idle_seconds']="0"
default['hazelcast']['map_eviction_policy']="NONE"
default['hazelcast']['map_max_size_policy']="0"
default['hazelcast']['map_eviction_percentage']="25"
default['hazelcast']['map_merge_policy']="hz.ADD_NEW_ENTRY"

default['hazelcast']['semaphore_enabled']=false
default['hazelcast']['semaphore_name']="default"
default['hazelcast']['semaphore_initial_permits']="10"
default['hazelcast']['semaphore_factory_enabled']="true"
default['hazelcast']['semaphore_factory_class_name']="com.acme.MySemaphoreFactory"


