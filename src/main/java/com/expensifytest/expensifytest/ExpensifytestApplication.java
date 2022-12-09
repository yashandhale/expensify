package com.expensifytest.expensifytest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.expensifytest.expensifytest.controller.AuthenticationController;
import com.expensifytest.expensifytest.model.Session;
import com.expensifytest.expensifytest.model.Vendor;
import com.expensifytest.expensifytest.model.VendorItem;
import com.expensifytest.expensifytest.repository.VendorRepository;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class ExpensifytestApplication {

	private static final int INTERVAL = 300000;

	@Autowired
	AuthenticationController authenticationController;

	@Autowired
	VendorRepository vendorRepository;

	public static void main(String[] args) {
		SpringApplication.run(ExpensifytestApplication.class, args);
	}

	@Scheduled(fixedDelay = INTERVAL)
	public void fetchVendors() throws ParseException {
		Session session = authenticationController.getSession();
		if (session != null) {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
			formData.add("requestJobDescription",
					"{'type': 'file','credentials':{'partnerUserID': '" + session.getUserId() + "','partnerUserSecret': '"
							+ session.getUserSecret()
							+ "'},'onReceive':{'immediateResponse':['returnRandomFileName']},'inputSettings':{'type': 'combinedReportData','filters':{'startDate':'"
							+ session.getLastFetched() + "'}},'outputSettings':{'fileExtension':'json'}}");
			formData.add("template",
					"<#list reports as report><#list report.transactionList as expense><#if expense.modifiedMerchant?has_content><#assign merchant = expense.modifiedMerchant><#else><#assign merchant = expense.merchant></#if><#if expense.convertedAmount?has_content><#assign amount = expense.convertedAmount/100><#elseif expense.modifiedAmount?has_content><#assign amount = expense.modifiedAmount/100><#else><#assign amount = expense.amount/100></#if><#if expense.modifiedCreated?has_content><#assign created = expense.modifiedCreated><#else><#assign created = expense.created></#if><#assign bank = expense.bank><#assign billable = expense.billable><#assign category = expense.category><#assign currency = expense.currency><#assign taxAmount = expense.taxAmount><#assign type = expense.type>${merchant},<#t>${amount},<#t>${created},<#t>${bank},<#t>${billable},<#t>${category},<#t>${currency},<#t>${taxAmount},<#t>${type};<#lt></#list></#list>");
			ResponseEntity<String> responseEntity = null;
			try {
				responseEntity = restTemplate.exchange(
						"https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations", HttpMethod.POST,
						new HttpEntity<MultiValueMap<String, String>>(formData, headers), String.class);
				String filename = responseEntity.getBody();
				formData = new LinkedMultiValueMap<String, String>();
				formData.add("requestJobDescription",
						"{'type': 'download','credentials': {'partnerUserID': '" + session.getUserId() + "' ,'partnerUserSecret': '"
								+ session.getUserSecret()
								+ "'},'fileName': '" + filename + "','fileSystem': 'integrationServer'}");
				responseEntity = restTemplate.exchange(
						"https://integrations.expensify.com/Integration-Server/ExpensifyIntegrations", HttpMethod.POST,
						new HttpEntity<MultiValueMap<String, String>>(formData, headers), String.class);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(
						new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z").parse(responseEntity.getHeaders().get("Date").get(0)));
				calendar.add(Calendar.DATE, -1);
				String fetchDate = new SimpleDateFormat("yyyy-MM-dd")
						.format(calendar.getTime())
						.toString();
				authenticationController.updateLastFetched(fetchDate);
				String[] expenses = responseEntity.getBody().split(";");
				for (String expense : expenses) {
					String[] values = expense.split(",");
					Optional<Vendor> existing = vendorRepository.findById(values[0]);
					Vendor vendor;
					if (existing.isPresent()) {
						vendor = existing.get();
						String vendorItemId = getVendorItemId(values);
						boolean itemExist = false;
						for (VendorItem vendorItem : vendor.getVendorItems()) {
							if (vendorItem.getId().equals(vendorItemId)) {
								itemExist = true;
								break;
							}
						}
						if (!itemExist)
							vendor.getVendorItems().add(getVendorItem(values));
					} else {
						vendor = new Vendor();
						vendor.setId(values[0]);
						vendor.setName(values[0]);
						ArrayList<VendorItem> vendorItems = new ArrayList<VendorItem>();
						vendorItems.add(getVendorItem(values));
						vendor.setVendorItems(vendorItems);
					}
					vendorRepository.save(vendor);
				}
			} catch (ResourceAccessException exception) {
				System.out.println(exception.getMessage());
			}
		}
	}

	private VendorItem getVendorItem(String[] values) {
		VendorItem vendorItem = new VendorItem();
		vendorItem.setId(getVendorItemId(values));
		vendorItem.setAmount(values[1]);
		vendorItem.setCreated(values[2]);
		vendorItem.setBank(values[3]);
		vendorItem.setBillable(values[4]);
		vendorItem.setCategory(values[5]);
		vendorItem.setCurrency(values[6]);
		vendorItem.setTaxAmount(values[7]);
		vendorItem.setType(values[8]);
		return vendorItem;
	}

	private String getVendorItemId(String[] values) {
		StringBuilder valueBuyilder = new StringBuilder();
		for (String value : values) {
			valueBuyilder.append(value);
		}
		return Base64.getEncoder().encodeToString(valueBuyilder.toString().getBytes());
	}
}
