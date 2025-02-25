# key pair name to be assigned to EC2 instance
aws_key_name = #"id_rsa"

# local path of public and private key file for SSH connection - local_key_path/aws_key_name
local_key_path = #"~/.ssh"


########### Optional ############
member_count = "2"

# If you are using free tier, using aws_instance_type other than "t2.micro" or "t3.micro" will cost money.
aws_instance_type      = "t2.micro"
aws_region             = "eu-central-1"
aws_tag_key            = "hz-guide"
aws_tag_value          = "terraform"
aws_connection_retries = "3"

prefix                      = "hazelcast"
hazelcast_mancenter_version = "4.2020.12"

# Username to use when connecting to VMs. Do not change this if you are using ubuntu images.
aws_ssh_user = "ubuntu"