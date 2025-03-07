# key name to be assigned to Azure Compute instances
azure_key_name = #"id_rsa"

# local path of private key file for SSH connection - local_key_path/azure_key_name
local_key_path = #"~/.ssh"



########### Optional ############
location            = "central us"
azure_instance_type = "Standard_B1ms"

member_count = "2"

prefix                      = "hazelcast"
hazelcast_mancenter_version = "4.2020.12"

# Username to use when connecting to VMs.
azure_tag_key = "hz-guide"
azure_tag_value = "terraform"
azure_ssh_user = "ubuntu"

 