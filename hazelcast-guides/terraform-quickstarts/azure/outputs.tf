output "members_public_ip" {
  value       = azurerm_linux_virtual_machine.hazelcast_member.*.public_ip_address
  description = "The public IPs of the Hazelcast Members"
}


output "mancenter_public_ip" {
  value       = azurerm_linux_virtual_machine.hazelcast_mancenter.public_ip_address
  description = "The public IP of the Hazelcast Management Center"
}
