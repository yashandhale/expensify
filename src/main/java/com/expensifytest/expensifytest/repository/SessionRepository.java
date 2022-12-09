package com.expensifytest.expensifytest.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.expensifytest.expensifytest.model.Session;

public interface SessionRepository extends MongoRepository<Session, String> {

}