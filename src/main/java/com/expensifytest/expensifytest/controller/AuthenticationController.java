package com.expensifytest.expensifytest.controller;

import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.expensifytest.ResponseObject;
import com.expensifytest.expensifytest.model.Session;
import com.expensifytest.expensifytest.repository.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class AuthenticationController {

  private static Session session;

  @Autowired
  SessionRepository sessionRepository;

  @RequestMapping("/authenticate")
  public int authenticate(String userID, String userSecret) throws JsonMappingException, JsonProcessingException {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
    formData.add("requestJobDescription",
        "{'type': 'get','credentials':{'partnerUserID': '" + userID + "','partnerUserSecret': '" + userSecret
            + "'},'inputSettings':{'type': 'policyList'}}");
    ResponseEntity<String> responseEntity = null;
    try {
      responseEntity = restTemplate.exchange(
          "https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations", HttpMethod.POST,
          new HttpEntity<MultiValueMap<String, String>>(formData, headers), String.class);
      ObjectMapper objectMapper = new ObjectMapper();
      ResponseObject responseObject = objectMapper.readValue(responseEntity.getBody(), ResponseObject.class);
      if (responseObject.authenticated()) {
        Random random = new Random();
        int id = random.nextInt(1000001);
        session = new Session();
        session.setId(String.valueOf(id));
        session.setUserId(userID);
        session.setUserSecret(userSecret);
        session.setLastFetched("2000-01-01");
        sessionRepository.deleteAll();
        sessionRepository.save(session);
        return id;
      } else {
        return 0;
      }
    } catch (ResourceAccessException exception) {
      return -1;
    }
  }

  @RequestMapping("/session")
  public boolean session(String sessionId) {
    if (session != null)
      return session.getId().equals(sessionId);
    return sessionRepository.findById(sessionId).isPresent();
  }

  public Session getSession() {
    if (session != null)
      return session;
    List<Session> sessions = sessionRepository.findAll();
    if (sessions.size() > 0) {
      return sessions.get(0);
    } else {
      return null;
    }
  }

  public void updateLastFetched(String date) {
    Session session = getSession();
    session.setLastFetched(date);
    sessionRepository.save(session);
  }
}