package com.logicoy.bpelmon.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ReprocessConfigModel;

@Service
public class ReprocessConfigDAOImpl extends JdbcDaoSupport implements ReprocessConfigDAO {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	private Logger reprocessLogger = Logger.getLogger(this.getClass().getName());

	@Override
	public List<ReprocessConfigDTO> getConfiguration(ReprocessConfigModel configModel) {
		reprocessLogger.info("Request body: " + configModel.toString());
		List<ReprocessConfigDTO> configList = null;
		String query = null;
		if (configModel.getType() == null && configModel.getBpelId() != null) {
			reprocessLogger.info("Transaction type is: " + configModel.getType());
			query = "SELECT * FROM reprocess_config WHERE client_id=? AND su_name=?";
			configList = jdbcTemplate.query(query, new Object[] { configModel.getClientId().trim(),
					configModel.getServiceName().toLowerCase().trim() }, (ResultSet rs, int i) -> {
						ReprocessConfigDTO config = new ReprocessConfigDTO();
						config.setClientId(rs.getString("client_id"));
						config.setBpelId(rs.getString("bpel_id"));
						config.setSuName(rs.getString("su_name"));
						config.setServiceEndpoint(rs.getString("service_endpoint"));
						config.setTransactionDataTemplate(rs.getString("transaction_data_template"));
						config.setTransactionType(rs.getString("transaction_type"));
						config.setUsername(rs.getString("username"));
						config.setPassword(rs.getString("password"));
						config.setMethod(rs.getString("method"));
						config.setContentType(rs.getString("content_type"));
						config.setAcceptType(rs.getString("accept_type"));
						config.setXslTransformation(rs.getString("xsl_transformation"));
						return config;
					});
		} else if (configModel.getType() != null) {
			reprocessLogger.info("Bpel id is null");
			query = "SELECT * FROM reprocess_config WHERE client_id=? AND su_name=?";
			configList = jdbcTemplate.query(query, new Object[] { configModel.getClientId().toLowerCase().trim(),
					configModel.getServiceName().trim() }, (ResultSet rs, int i) -> {
						ReprocessConfigDTO config = new ReprocessConfigDTO();
						config.setClientId(rs.getString("client_id"));
						config.setBpelId(rs.getString("bpel_id"));
						config.setSuName(rs.getString("su_name"));
						config.setServiceEndpoint(rs.getString("service_endpoint"));
						config.setTransactionDataTemplate(rs.getString("transaction_data_template"));
						config.setTransactionType(rs.getString("transaction_type"));
						config.setUsername(rs.getString("username"));
						config.setPassword(rs.getString("password"));
						config.setMethod(rs.getString("method"));
						config.setContentType(rs.getString("content_type"));
						config.setAcceptType(rs.getString("accept_type"));
						config.setXslTransformation(rs.getString("xsl_transformation"));
						return config;
					});
		} else {
			reprocessLogger.info("Only Su name present");
			query = "SELECT * FROM reprocess_config WHERE client_id=? AND su_name=?";
			configList = jdbcTemplate.query(query,
					new Object[] { configModel.getClientId().trim(), configModel.getServiceName().trim() },
					(ResultSet rs, int i) -> {
						ReprocessConfigDTO config = new ReprocessConfigDTO();
						config.setClientId(rs.getString("client_id"));
						config.setBpelId(rs.getString("bpel_id"));
						config.setSuName(rs.getString("su_name"));
						config.setServiceEndpoint(rs.getString("service_endpoint"));
						config.setTransactionDataTemplate(rs.getString("transaction_data_template"));
						config.setTransactionType(rs.getString("transaction_type"));
						config.setUsername(rs.getString("username"));
						config.setPassword(rs.getString("password"));
						config.setMethod(rs.getString("method"));
						config.setContentType(rs.getString("content_type"));
						config.setAcceptType(rs.getString("accept_type"));
						config.setXslTransformation(rs.getString("xsl_transformation"));
						return config;
					});
		}

		reprocessLogger.info("Configuration list: " + configList.size());
		return configList;
	}

	@Override
	public GenericResponseModel updateConfiguration(ReprocessConfigDTO config) {
		GenericResponseModel model = new GenericResponseModel();
		String query = "UPDATE reprocess_config SET transaction_type=?,transaction_data_template=?,service_endpoint=?,username=?,password=?,method=?,content_type=?,accept_type=?,xsl_transformation=? WHERE client_id=? AND su_name=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(query, new Object[] {config.getTransactionType(),config.getTransactionDataTemplate(), config.getServiceEndpoint(), config.getUsername(),
					config.getPassword(), config.getMethod(), config.getContentType(), config.getAcceptType(), config.getXslTransformation(), config.getClientId().trim(), config.getSuName() });
		} catch (Exception e) {
			reprocessLogger.severe("Exception: " + e.getMessage() + "  Caused by: " + e.getCause());
		}
		if (affectedRows > 0) {
			model.setStatus(1);
			model.setMessage("Configuration updated");
		} else {
			model.setStatus(0);
			model.setMessage("No rows updated");
		}
		return model;
	}

	@Override
	public GenericResponseModel addConfiguration(ReprocessConfigDTO config) {
		
		GenericResponseModel resp = new GenericResponseModel();
		resp.setStatus(0);
		resp.setMessage("Something went wrong! Please try again");
		
		String insertSQL = "INSERT INTO reprocess_config VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] args = new Object[] {
				config.getClientId(),
				config.getSuName(),
				config.getBpelId(),
				config.getTransactionType(),
				config.getTransactionDataTemplate(),
				config.getServiceEndpoint(),
				config.getUsername(),
				config.getPassword(),
				config.getMethod(),
				config.getContentType(),
				config.getAcceptType(),
				config.getXslTransformation()
				};
		int affectedRows = jdbcTemplate.update(insertSQL, args);
		resp.setStatus(1);
		resp.setMessage("Updated " + affectedRows + " row(s)");
		return resp;
	}

	@Override
	public List<ReprocessConfigDTO> getConfigurationForClient(String clientId) {
		String sql = "SELECT * FROM reprocess_config WHERE client_id=?";
		return jdbcTemplate.query(sql, new Object[] {clientId}, new RowMapper<ReprocessConfigDTO>() {

			@Override
			public ReprocessConfigDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReprocessConfigDTO config = new ReprocessConfigDTO();
				config.setClientId(rs.getString("client_id"));
				config.setBpelId(rs.getString("bpel_id"));
				config.setSuName(rs.getString("su_name"));
				config.setServiceEndpoint(rs.getString("service_endpoint"));
				config.setTransactionDataTemplate(rs.getString("transaction_data_template"));
				config.setTransactionType(rs.getString("transaction_type"));
				config.setUsername(rs.getString("username"));
				config.setPassword(rs.getString("password"));
				config.setMethod(rs.getString("method"));
				config.setContentType(rs.getString("content_type"));
				config.setAcceptType(rs.getString("accept_type"));
				config.setXslTransformation(rs.getString("xsl_transformation"));
				return config;
			}});
	}
	
}
