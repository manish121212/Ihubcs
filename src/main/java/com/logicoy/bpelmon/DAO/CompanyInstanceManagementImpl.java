package com.logicoy.bpelmon.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.models.CompanyInstanceDTO;
import com.logicoy.bpelmon.models.CompanyInstanceListResponse;
import com.logicoy.bpelmon.models.UpdateCompanyInstanceMappingDTO;

@Repository
public class CompanyInstanceManagementImpl extends JdbcDaoSupport implements CompanyInstanceManagement {
	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public CompanyInstanceListResponse getCompanyInstanceList(int companyId) {
		String QUERY = "SELECT cd.Id, cd.company_name, id.* FROM company_instance_mapping cim inner join company_details cd ON cd.Id=cim.company_id inner join instance_details id on id.id=cim.instance_id where company_id=?";
		boolean isCompanyDetailsSet = false;
		CompanyInstanceListResponse response = new CompanyInstanceListResponse();
		List<CompanyInstanceDTO> instanceList = jdbcTemplate.query(QUERY, new Object[] { companyId },
				new RowMapper<CompanyInstanceDTO>() {

					@Override
					public CompanyInstanceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
						CompanyInstanceDTO dto = new CompanyInstanceDTO();

						if (!isCompanyDetailsSet) {
							response.setCompanyId(rs.getInt("cd.Id"));
							response.setCompanyName(rs.getString("cd.company_name"));
						}
						dto.setCreatedAt(rs.getString("id.created_at"));
						dto.setEsClientName(rs.getString("id.es_client_name"));
						dto.setId(rs.getInt("id.id"));
						dto.setInstanceDetails(rs.getString("id.instance_details"));
						dto.setInstanceName(rs.getString("id.instance_name"));

						return dto;
					}
				});
		response.setInstanceList(instanceList);
		return response;
	}

	@Override
	public CompanyInstanceListResponse getCompanyInstanceList(String companyName) {
		String QUERY = "SELECT cd.Id, cd.company_name, id.* FROM company_instance_mapping cim inner join company_details cd ON cd.Id=cim.company_id inner join instance_details id on id.id=cim.instance_id where company_name=?";
		boolean isCompanyDetailsSet = false;
		CompanyInstanceListResponse response = new CompanyInstanceListResponse();
		List<CompanyInstanceDTO> instanceList = jdbcTemplate.query(QUERY, new Object[] { companyName },
				new RowMapper<CompanyInstanceDTO>() {

					@Override
					public CompanyInstanceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
						CompanyInstanceDTO dto = new CompanyInstanceDTO();

						if (!isCompanyDetailsSet) {
							response.setCompanyId(rs.getInt("cd.Id"));
							response.setCompanyName(rs.getString("cd.company_name"));
						}
						dto.setCreatedAt(rs.getString("id.created_at"));
						dto.setEsClientName(rs.getString("id.es_client_name"));
						dto.setId(rs.getInt("id.id"));
						dto.setInstanceDetails(rs.getString("id.instance_details"));
						dto.setInstanceName(rs.getString("id.instance_name"));

						return dto;
					}
				});
		response.setInstanceList(instanceList);
		return response;
	}

	@Override
	public String addCompanyInstance(int companyId, List<CompanyInstanceDTO> instanceList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addCompanyInstance(String companyName, List<CompanyInstanceDTO> instanceList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CompanyInstanceDTO> getInstanceList() {
		String SQL = "SELECT * from instance_details id";
		return jdbcTemplate.query(SQL, new RowMapper<CompanyInstanceDTO>() {

			@Override
			public CompanyInstanceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				CompanyInstanceDTO dto = new CompanyInstanceDTO();

				dto.setCreatedAt(rs.getString("created_at"));
				dto.setEsClientName(rs.getString("es_client_name"));
				dto.setId(rs.getInt("id"));
				dto.setInstanceDetails(rs.getString("instance_details"));
				dto.setInstanceName(rs.getString("instance_name"));
				return dto;
			}
		});
	}

	@Override
	public boolean addNewInstance(CompanyInstanceDTO dto) {
		// Get company id
		int companyId = Integer.parseInt(dto.getCompanyMapped());
		// Save dto in db
		String SQL = "INSERT INTO instance_details (instance_name,es_client_name, instance_details) VALUES(?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement ps = connection.prepareStatement(SQL, new String[] { "id" });
				ps.setString(1, dto.getInstanceName());
				ps.setString(2, dto.getEsClientName());
				ps.setString(3, dto.getInstanceDetails());
				return ps;
			}
		}, keyHolder);
		// Get generated id
		int savedId = keyHolder.getKey().intValue();
		logger.info("Saved instance id: " + savedId);
		// Insert this id with company id in mapping
		return insertCompanyInstanceMapping(companyId, savedId);
	}
	public boolean insertCompanyInstanceMapping(int companyId, int instanceId) {
		logger.info("Check if existing pair is present");
		String SQL_CHECK = "SELECT * FROM company_instance_mapping where company_id=? AND instance_id=?";
		boolean isMappingPresent = jdbcTemplate.query(SQL_CHECK, new Object[] {companyId, instanceId}, new ResultSetExtractor<Boolean>() {
			@Override
			public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
				
				return rs.next();
			}});
		if(isMappingPresent) {
			logger.info("Mapping already present. Return false");
			return false;
		} else {
			logger.info("Mapping doesn't exist create new.");
			String SQL = "INSERT INTO company_instance_mapping VALUES(?,?)";
			int rows = jdbcTemplate.update(SQL, companyId, instanceId);
			return rows > 0 ? true : false;
		}
	}

	@Override
	public boolean updateCompanyInstanceMapping(UpdateCompanyInstanceMappingDTO dto) {
		// Get company id
		int companyId = dto.getCompanyId();
		logger.info("Remove all existing mapping for company");
		String SQL_DELETE = "DELETE FROM company_instance_mapping where company_id=?";
		int rows = jdbcTemplate.update(SQL_DELETE, companyId);
		logger.info("Deleted " + rows + " records");
		// Add all mapping from incoming instance list
		for(CompanyInstanceDTO instance: dto.getInstanceList()) {
			// Insert mapping for every record
			boolean res = insertCompanyInstanceMapping(companyId, instance.getId());
			logger.info("Insert status for company: " + companyId + " and instance: " + instance.getInstanceName() + " is " + res);
		}
		// Remove all 
		return true;
	}
}
