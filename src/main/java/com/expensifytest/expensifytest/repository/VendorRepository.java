package com.expensifytest.expensifytest.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.expensifytest.expensifytest.model.Vendor;

public interface VendorRepository extends MongoRepository<Vendor, String> {
}