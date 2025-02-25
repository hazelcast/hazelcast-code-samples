New-NetFirewallRule -DisplayName 'Hazelcast ports 5701-5703' `
  -Name Hazelcast -Direction Inbound -Action Allow `
  -Protocol TCP -LocalPort 5701-5703
