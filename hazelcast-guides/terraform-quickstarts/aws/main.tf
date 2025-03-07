terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = " 3.22.0"
    }
  }
  required_version = ">= 0.13"
}

provider "aws" {
  region = var.aws_region
}

data "aws_ami" "image" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-bionic-18.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"]
}

#################### IAM role creation for discovery ###################

resource "aws_iam_role" "discovery_role" {
  name = "${var.prefix}_discovery_role"

  assume_role_policy = <<-EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Action": "sts:AssumeRole",
        "Principal": {
          "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow",
        "Sid": ""
      }
    ]
  }
  EOF
}

resource "aws_iam_role_policy" "discovery_policy" {
  name = "${var.prefix}_discovery_policy"
  role = aws_iam_role.discovery_role.id

  policy = <<-EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Action": [
          "ec2:DescribeInstances"
        ],
        "Effect": "Allow",
        "Resource": "*"
      }
    ]
  }
  EOF
}

resource "aws_iam_instance_profile" "discovery_instance_profile" {
  name = "${var.prefix}_discovery_instance_profile"
  role = aws_iam_role.discovery_role.name
}


#################### Security Group for Allowing Access ###################

resource "aws_security_group" "sg" {
  name = "${var.prefix}_sg"

  #Allow SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  #Allow Hazelcast members to communicate
  ingress {
    from_port   = 5701
    to_port     = 5701
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  #Allow access to Management Center
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow outgoing traffic to anywhere.
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

#################### Key for Allowing SSH Access ###################
resource "aws_key_pair" "keypair" {
  key_name   = "${var.prefix}_${var.aws_key_name}"
  public_key = file("${var.local_key_path}/${var.aws_key_name}.pub")
}

##################### Hazelcast Cluster Member Instances ###################

resource "aws_instance" "hazelcast_member" {
  count                = var.member_count
  ami                  = data.aws_ami.image.id
  instance_type        = var.aws_instance_type
  iam_instance_profile = aws_iam_instance_profile.discovery_instance_profile.name
  security_groups      = [aws_security_group.sg.name]
  key_name             = aws_key_pair.keypair.key_name
  tags = {
    Name                 = "${var.prefix}-aws-member-${count.index}"
    "${var.aws_tag_key}" = var.aws_tag_value
  }

  connection {
    type        = "ssh"
    user        = var.aws_ssh_user
    host        = self.public_ip
    timeout     = "120s"
    agent       = false
    private_key = file("${var.local_key_path}/${var.aws_key_name}")
  }

  provisioner "file" {
    source      = "scripts/start_aws_hazelcast_member.sh"
    destination = "/home/${var.aws_ssh_user}/start_aws_hazelcast_member.sh"
  }

  provisioner "file" {
    source      = "hazelcast.yaml"
    destination = "/home/${var.aws_ssh_user}/hazelcast.yaml"
  }

  provisioner "remote-exec" {
    inline = [
      "while [ ! -f /var/lib/cloud/instance/boot-finished ]; do echo 'Waiting for cloud-init...'; sleep 1; done",
      "wget -qO - https://bintray.com/user/downloadSubjectPublicKey?username=hazelcast | sudo apt-key add -",
      "echo \"deb http://dl.bintray.com/hazelcast/deb stable main\" | sudo tee -a /etc/apt/sources.list",
      "sudo apt update && sudo apt -y install hazelcast",
      "sleep 10"
    ]
  }

  provisioner "remote-exec" {
    inline = [
      "cd /home/${var.aws_ssh_user}",
      "chmod 0755 start_aws_hazelcast_member.sh",
      "./start_aws_hazelcast_member.sh ${var.aws_region} ${var.aws_tag_key} ${var.aws_tag_value} ${var.aws_connection_retries} ${aws_iam_role.discovery_role.name}",
      "sleep 10",
      "tail -n 10 /home/${var.aws_ssh_user}/hazelcast.stdout.log"
    ]
  }
}

##################### Hazelcast Management Center ###################

resource "aws_instance" "hazelcast_mancenter" {
  ami                  = data.aws_ami.image.id
  instance_type        = var.aws_instance_type
  iam_instance_profile = aws_iam_instance_profile.discovery_instance_profile.name
  security_groups      = [aws_security_group.sg.name]
  key_name             = aws_key_pair.keypair.key_name
  tags = {
    Name                 = "${var.prefix}-aws-management-center"
    "${var.aws_tag_key}" = var.aws_tag_value
  }

  connection {
    type        = "ssh"
    user        = var.aws_ssh_user
    host        = self.public_ip
    timeout     = "120s"
    agent       = false
    private_key = file("${var.local_key_path}/${var.aws_key_name}")
  }

  provisioner "file" {
    source      = "scripts/start_aws_hazelcast_management_center.sh"
    destination = "/home/${var.aws_ssh_user}/start_aws_hazelcast_management_center.sh"
  }

  provisioner "file" {
    source      = "hazelcast-client.yaml"
    destination = "/home/${var.aws_ssh_user}/hazelcast-client.yaml"
  }

  provisioner "remote-exec" {
    inline = [
      "while [ ! -f /var/lib/cloud/instance/boot-finished ]; do echo 'Waiting for cloud-init...'; sleep 1; done",
      "sudo apt-get update",
      "sudo apt-get -y install openjdk-8-jdk wget unzip",
      "sleep 10"
    ]
  }

  provisioner "remote-exec" {
    inline = [
      "cd /home/${var.aws_ssh_user}",
      "chmod 0755 start_aws_hazelcast_management_center.sh",
      "./start_aws_hazelcast_management_center.sh ${var.hazelcast_mancenter_version}  ${var.aws_region} ${var.aws_tag_key} ${var.aws_tag_value} ",
      "sleep 10",
      "tail -n 20 ./logs/mancenter.stdout.log"
    ]
  }
}

