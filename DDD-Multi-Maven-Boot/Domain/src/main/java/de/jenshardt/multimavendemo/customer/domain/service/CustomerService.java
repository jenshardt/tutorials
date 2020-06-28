package de.jenshardt.multimavendemo.customer.domain.service;

import java.util.UUID;

import de.jenshardt.multimavendemo.customer.domain.aggregate.Customer;

public interface CustomerService {
	public Customer getCustomerById(UUID customerId);
	public Customer createCustomer(String name, String job);
}
