package com.expensifytest;

public class ResponseObject {
  private String responseMessage;
  private String responseCode;
  private PolicyObject[] policyList;

  public PolicyObject[] getPolicyList() {
    return policyList;
  }

  public void setPolicyList(PolicyObject[] policyList) {
    this.policyList = policyList;
  }

  public String getResponseMessage() {
    return responseMessage;
  }

  public void setResponseMessage(String responseMessage) {
    this.responseMessage = responseMessage;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  public boolean authenticated() {
    return responseCode.equals("200");
  }
}
