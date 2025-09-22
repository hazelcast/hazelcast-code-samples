package com.hazelcast.samples.testing.junit5;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.HzCustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceMockitoTest {

    @Mock
    IMap<String, Customer> customerMap;

    @Mock
    HazelcastInstance hzInstance;

    @InjectMocks
    HzCustomerService service;

    @Test
    void findCustomerWithMock() {
        //noinspection unchecked,rawtypes
        when(hzInstance.getMap("customers")).thenReturn((IMap) customerMap);
        when(customerMap.get("123")).thenReturn(new Customer("123", "Alice"));

        assertEquals("Alice", service.findCustomer("123").name());
    }
}
