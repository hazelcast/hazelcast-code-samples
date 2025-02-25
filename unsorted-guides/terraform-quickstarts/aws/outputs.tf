output "members_public_ip" {
  value       = aws_instance.hazelcast_member.*.public_ip
  description = "The public IPs of the Hazelcast Members"
}


output "mancenter_public_ip" {
  value       = aws_instance.hazelcast_mancenter.public_ip
  description = "The public IP of the Hazelcast Management Center"
}
