# key name to be assigned to Google Compute instances
variable "gcp_key_name" {
  type = string
}

# local path of private key file for SSH connection - local_key_path/gcp_key_name
variable "local_key_path" {
  type = string
}

# ID of the project you want to use
variable "project_id" {
  type = string
}

variable "region" {
  type    = string
  default = "us-central1"
}

variable "zone" {
  type    = string
  default = "us-central1-c"
}

variable "member_count" {
  type    = number
  default = "2"
}

variable "gcp_ssh_user" {
  type    = string
  default = "ubuntu"
}

variable "hazelcast_mancenter_version" {
  type    = string
  default = "4.2020.12"
}

variable "prefix" {
  type    = string
  default = "hazelcast"
}

variable "gcp_instance_type" {
  type    = string
  default = "f1-micro"
}

variable "gcp_label_key" {
  type    = string
  default = "hz-guide"
}

variable "gcp_label_value" {
  type    = string
  default = "terraform"
}
