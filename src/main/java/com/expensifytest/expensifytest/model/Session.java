package com.expensifytest.expensifytest.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("session")
public class Session {
  @Id
  private String id;
  private String userId;
  private String userSecret;
  private String lastFetched;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserSecret() {
    return userSecret;
  }

  public void setUserSecret(String userSecret) {
    this.userSecret = userSecret;
  }

  public String getLastFetched() {
    return lastFetched;
  }

  public void setLastFetched(String lastFetched) {
    this.lastFetched = lastFetched;
  }
}