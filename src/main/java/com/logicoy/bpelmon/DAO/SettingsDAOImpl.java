package com.logicoy.bpelmon.DAO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ServiceNameResolverModel;
import com.logicoy.bpelmon.utils.AppConstants;

@Repository
public class SettingsDAOImpl extends JdbcDaoSupport implements SettingsDAO {

	@Autowired
	DataSource dataSource;
	@Autowired
	AppConstants appConst;

	@PostConstruct
	private void init() {
		setDataSource(dataSource);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;
	private static final Logger LOGGER = Logger.getLogger(SettingsDAOImpl.class.getName());

	@Override
	public GenericResponseModel updateCompanyDetails(CompanyDetails updatedCompany) {
		GenericResponseModel resp = new GenericResponseModel();
		resp.setStatus(0);
		resp.setMessage("Something went wrong! Please try again");
		LOGGER.log(Level.INFO, "Company to be updated {0}", updatedCompany.toString());
		String updateCompanySQL = "UPDATE company_details SET website=?,contact_no=?,contact_email=?,timezone=?,email_alert=? WHERE id=? ORDER BY company_name ASC";
		Object[] args = new Object[] { updatedCompany.getWebsite(),
				updatedCompany.getContactNo(), updatedCompany.getContactEmail(), updatedCompany.getTimezone(),
				updatedCompany.getEmailAlert(), updatedCompany.getId() };

		int affectedRows = jdbcTemplate.update(updateCompanySQL, args);
		resp.setStatus(1);
		resp.setMessage("Updated " + affectedRows + " row(s)");
		return resp;
	}

	@Override
	public List<ServiceNameResolverModel> getServiceDisplayNameList(String clientId) {
		String configFilePath = appConst.getFrontendConfigFolder();
		LOGGER.log(Level.INFO, "Base folder for frontend config files {0}", configFilePath);
		// File serviceNameResolverFile = new File(configFilePath +
		// "service_name_resolver.json");
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configFilePath));
			String jsonContent = new String(encoded, Charset.defaultCharset());
			// LOGGER.log(Level.INFO, "File content\n {0}", jsonContent);
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonContent, new TypeReference<List<ServiceNameResolverModel>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public GenericResponseModel updateServiceDisplayNameList(ServiceNameResolverModel model) {
		GenericResponseModel resp = new GenericResponseModel();
		resp.setStatus(0);
		resp.setMessage("Something went wrong! Please try again");

		String configFilePath = appConst.getFrontendConfigFolder();
		LOGGER.log(Level.INFO, "Frontend config file: {0}", configFilePath);
		List<ServiceNameResolverModel> configuredServiceList = this.getServiceDisplayNameList("");

		ServiceNameResolverModel itemtoBeUpdated = configuredServiceList.stream()
				.filter(p -> p.getClientId().equalsIgnoreCase(model.getClientId())).findAny().orElse(null);
		// LOGGER.log(Level.INFO, "Item to be updated {0}", itemtoBeUpdated.toString());

		configuredServiceList.remove(itemtoBeUpdated);

		configuredServiceList.add(model);
		try (FileWriter fWriter = new FileWriter(configFilePath, false)) {
			String jsonString = new ObjectMapper().writeValueAsString(configuredServiceList);
			fWriter.write(jsonString);
			resp.setStatus(1);
			resp.setMessage("Updated configuration");
		} catch (IOException e) {
			e.printStackTrace();
			resp.setMessage(e.getMessage());
		}

		return resp;
	}

	@Override
	public GenericResponseModel addServiceDisplayNameList(ServiceNameResolverModel model) {
		GenericResponseModel resp = new GenericResponseModel();
		resp.setStatus(0);
		resp.setMessage("Something went wrong! Please try again");

		String configFilePath = appConst.getFrontendConfigFolder();
		LOGGER.log(Level.INFO, "Base folder for frontend config files {0}", configFilePath);
		List<ServiceNameResolverModel> configuredServiceList = this.getServiceDisplayNameList("");

		ServiceNameResolverModel itemtoBeUpdated = configuredServiceList.stream()
				.filter(p -> p.getClientId().equalsIgnoreCase(model.getClientId())).findAny().orElse(null);

		if (itemtoBeUpdated != null) {
			// Configuration already exits. Do not insert.
			resp.setStatus(1);
			resp.setMessage("Configuration already exists for this client. Please use update instead.");
		} else {
			// Configuration not found. Add in list and save
			configuredServiceList.add(model);
			try (FileWriter fWriter = new FileWriter(configFilePath, false)) {
				String jsonString = new ObjectMapper().writeValueAsString(configuredServiceList);
				fWriter.write(jsonString);
				resp.setStatus(1);
				resp.setMessage("Updated configuration");
			} catch (IOException e) {
				e.printStackTrace();
				resp.setMessage(e.getMessage());
			}
		}

		return resp;
	}

}
