output "members_public_ip" {
  value       = google_compute_instance.hazelcast_member.*.network_interface.0.access_config.0.nat_ip
  description = "The public IPs of the Hazelcast Members"
}


output "mancenter_public_ip" {
  value       = google_compute_instance.hazelcast_mancenter.network_interface.0.access_config.0.nat_ip
  description = "The public IP of the Hazelcast Management Center"
}
