# key name to be assigned to Google Compute instances
gcp_key_name = #"id_rsa"

# local path of private key file for SSH connection - local_key_path/gcp_key_name
local_key_path = #"~/.ssh"

# ID of the project you want to use
project_id =  #"project_id"

########### Optional ############

region = "us-central1"
zone   = "us-central1-c"

member_count      = "2"
gcp_instance_type = "f1-micro"

prefix                      = "hazelcast"
hazelcast_mancenter_version = "4.2020.12"

gcp_label_key = "hz-guide"
gcp_label_value = "terraform"

# Username to use when connecting to VMs
gcp_ssh_user = "ubuntu"
