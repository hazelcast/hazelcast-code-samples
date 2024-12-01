package com.hazelcast.samples.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Employee {
    private Integer empId;
    private String name;
    private Double salary;
}
