package com.logicoy.bpelmon.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.TimeZoneDTO;

@Repository
public class CompanyManagementImpl extends JdbcDaoSupport implements CompanyManagement {

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	Logger LOGGER = Logger.getLogger(this.getClass());

	@Override
	public List<CompanyDetails> getAllCompanyDetails() {
		String getAllCompanyDetailsQuery = "select * from company_details order by company_name asc";
		try {
			List<CompanyDetails> companyDetailsList = jdbcTemplate.query(getAllCompanyDetailsQuery,
					new RowMapper<CompanyDetails>() {
						@Override
						public CompanyDetails mapRow(ResultSet rs, int i) throws SQLException {
							CompanyDetails companyDetails = new CompanyDetails();
							companyDetails.setId(rs.getInt("Id"));
							companyDetails.setCompanyName(rs.getString("company_name"));
							companyDetails.setWebsite(rs.getString("website"));
							companyDetails.setContactEmail(rs.getString("contact_email"));
							companyDetails.setContactNo(rs.getString("contact_no"));
							companyDetails.setTimezone(rs.getString("timezone"));
							companyDetails.setPayloadStatus(rs.getInt("payload_status"));
							companyDetails.setEmailAlert(rs.getString("email_alert"));
							return companyDetails;
						}
					});
			return companyDetailsList;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	@Override
	public int addCompanyDetails(CompanyDetails companyDetails) {

		String checkCompanyExist = "select * from company_details;";
		try {

			List<Map<String, Object>> comp_Data = jdbcTemplate.queryForList(checkCompanyExist);

			for (Map<String, Object> company : comp_Data) {
				if (company.get("company_name").equals(companyDetails.getCompanyName())) {
					LOGGER.info("company exist in db");
					return 3; // 3 for username already exist
				} else if (company.get("contact_no").equals(companyDetails.getContactNo())) {
					LOGGER.info("Company contact number in db");
					return 5; // 5 for email already exist for user
				} else if (company.get("contact_email").equals(companyDetails.getContactEmail())) {
					LOGGER.info("Company email exist in db");
					return 4; // 4 for email already exist for user
				} else if (company.get("website").equals(companyDetails.getWebsite())) {
					LOGGER.info("Company website exist in db");
					return 6; // 6 for email already exist for user
				}
			}
			LOGGER.info("Storing company details");
			// Inserting data in the company details
			String insertCompanyQuery = "insert into company_details(company_name,website,contact_no,contact_email, timezone, payload_status) values (?,?,?,?, ?, ?);";
			int isCompanyInsertSuccessful;
			try {
				isCompanyInsertSuccessful = jdbcTemplate.update(insertCompanyQuery,
						new Object[] { companyDetails.getCompanyName(), companyDetails.getWebsite(),
								companyDetails.getContactNo(), companyDetails.getContactEmail(),
								companyDetails.getTimezone(), companyDetails.getPayloadStatus() });

			} catch (Exception ee) {
				LOGGER.error(ee.getMessage());
				return 2; // for internal server error
			}
			return isCompanyInsertSuccessful;
		} catch (Exception oo) {
			LOGGER.error(oo.getMessage());
			return 2;
		}
	}

	@Override
	public List<TimeZoneDTO> getTimezoneList() {
		String getTimeZoneQuery = "select * from timezone";
		List<TimeZoneDTO> timezoneList = jdbcTemplate.query(getTimeZoneQuery, new RowMapper<TimeZoneDTO>() {

			@Override
			public TimeZoneDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				TimeZoneDTO timeZoneDTO = new TimeZoneDTO();
				timeZoneDTO.setTzCode(rs.getString("tz_code"));
				timeZoneDTO.setTzDifference(rs.getString("tz_difference"));
				timeZoneDTO.setTzLocation(rs.getString("tz_location"));
				return timeZoneDTO;
			}
		});
		return timezoneList;
	}

	@Override
	public TimeZoneDTO getTimeZoneForCode(String tzCode) {
		String getTimeZoneObjectQuery = "select * from timezone where tz_code=?";
		TimeZoneDTO timezoneDTO = jdbcTemplate.query(getTimeZoneObjectQuery, new Object[] { tzCode },
				new ResultSetExtractor<TimeZoneDTO>() {

					@Override
					public TimeZoneDTO extractData(ResultSet rs) throws SQLException, DataAccessException {
						TimeZoneDTO obj = new TimeZoneDTO();
						if (rs.next()) {
							obj.setTzCode(rs.getString("tz_code"));
							obj.setTzDifference(rs.getString("tz_difference"));
							obj.setTzLocation(rs.getString("tz_location"));
						}
						return obj;
					}
				});
		return timezoneDTO;
	}
}
