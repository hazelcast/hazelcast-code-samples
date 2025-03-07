# key pair name to be assigned to EC2 instance, it will be created by terraform.
variable "aws_key_name" {
  type = string
}

# local path of private key file for SSH connection - local_key_path/aws_key_name
variable "local_key_path" {
  type = string
}

variable "member_count" {
  type    = number
  default = "2"
}

variable "prefix" {
  type    = string
  default = "hazelcast"
}

# If you are using free tier, changing this to other than "t3.micro" will cost money.
variable "aws_instance_type" {
  type    = string
  default = "t2.micro"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "aws_tag_key" {
  type    = string
  default = "Category"
}

variable "aws_tag_value" {
  type    = string
  default = "hazelcast-aws-discovery"
}

variable "aws_connection_retries" {
  type    = number
  default = "3"
}

variable "hazelcast_mancenter_version" {
  type    = string
  default = "4.2020.12"
}

variable "aws_ssh_user" {
  type    = string
  default = "ubuntu"
}
