package com.expensifytest.expensifytest.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("vendor")
public class Vendor {
  @Id
  private String id;
  private String name;
  private ArrayList<VendorItem> vendorItems;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<VendorItem> getVendorItems() {
    return vendorItems;
  }

  public void setVendorItems(ArrayList<VendorItem> vendorItems) {
    this.vendorItems = vendorItems;
  }
}
