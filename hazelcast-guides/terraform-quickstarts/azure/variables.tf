# key name to be assigned to Azure Compute instances
variable "azure_key_name" {
  type = string
}

# local path of private and public key file for SSH connection - local_key_path/azure_key_name
variable "local_key_path" {
  type = string
}

variable "location" {
  type    = string
  default = "central us"
}

variable "prefix" {
  type    = string
  default = "hazelcast"
}

variable "member_count" {
  type    = number
  default = "2"
}

variable "hazelcast_mancenter_version" {
  type    = string
  default = "4.2020.12"
}

variable "azure_ssh_user" {
  type    = string
  default = "ubuntu"
}

variable "azure_instance_type" {
  type    = string
  default = "Standard_B1ms"
}

variable "azure_tag_key" {
  type    = string
  default = "hz-guide"
}

variable "azure_tag_value" {
  type    = string
  default = "terraform"
}
