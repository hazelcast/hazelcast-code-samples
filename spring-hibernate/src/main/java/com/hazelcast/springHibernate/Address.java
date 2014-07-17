package com.hazelcast.springHibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

@Embeddable
public class Address implements Serializable {

    private static final long serialVersionUID = 1856862670651243395L;

    @Column(name="STREET_NAME", unique = false, nullable = false)
    private String streetName;

    @Column(name="CITY", unique = false, nullable = false)
    private String city;

    @Column(name="COUNTRY", unique = false, nullable = false)
    private String country;

    public Address() {

    }

    public Address(String streetName, String city, String country) {
        this.streetName = streetName;
        this.city = city;
        this.country = country;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address [streetName=" + streetName + ", city=" + city
                + ", country=" + country + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result
                + ((streetName == null) ? 0 : streetName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country))
            return false;
        if (streetName == null) {
            if (other.streetName != null)
                return false;
        } else if (!streetName.equals(other.streetName))
            return false;
        return true;
    }

}