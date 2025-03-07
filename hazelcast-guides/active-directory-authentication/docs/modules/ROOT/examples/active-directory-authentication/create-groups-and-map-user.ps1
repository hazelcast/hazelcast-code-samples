New-ADGroup -Name "Acme Cache" -GroupScope Global `
  -Description "Cache users"
New-ADGroup -Name "Acme Cache Czechia" -GroupScope Global `
  -Description "Cache users in the Czech Republic"

Add-ADGroupMember -Identity "Acme Cache" -Members "Acme Cache Czechia"
Add-ADGroupMember -Identity "Acme Cache Czechia" -Members hazelcast
