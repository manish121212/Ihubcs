package com.logicoy.bpelmon.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.logicoy.bpelmon.models.FileDetails;
import com.logicoy.bpelmon.models.FtpClientModel;
import com.logicoy.bpelmon.models.FtpConfigurationModel;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.services.LFTPClient;
import com.logicoy.bpelmon.services.LSFTPClient;

@Service
public class FTPServiceRequestImpl implements FTPServiceRequest {
	@Autowired
	JdbcTemplate jdbcTemplate;
//	private Logger LOGGER = Logger.getLogger(this.getClass().getName());

	@Override
	public List<FileDetails> getFileList(RequestModel reqModel) {
		// Inititalize FTP or SFTP client
		FtpClientModel ftpClient = reqModel.getFtpDetails();
		
		
		if (ftpClient != null) {
			String protocol = ftpClient.getProtocol();
			if(protocol.equalsIgnoreCase("ftp")) {
			LFTPClient client = new LFTPClient(ftpClient.getHost(), ftpClient.getPort(), ftpClient.getUsername(),
					ftpClient.getPassword());
			if (client.connect()) {
				if (client.login()) {
					List<FileDetails> fileList = client.receiveFileList(reqModel.getFilePath());
					client.disconnect();
					return fileList;
				}
			}
			client.disconnect();
			} else {
				LSFTPClient client = new LSFTPClient(ftpClient.getHost(), ftpClient.getPort(), ftpClient.getUsername(), ftpClient.getPassword());
				if(client.connect()) {
					if(client.login()) {
						List<FileDetails> fileList = client.listFiles(reqModel.getFilePath(), "*");
						client.disconnect();
						return fileList;
					}
				}
			}
			
		}
		return null;
	}

	@Override
	public List<FtpConfigurationModel> getConfiguration(RequestModel reqModel) {
		String query = "SELECT * from ftp_monitoring_config WHERE client_id=?";
		List<FtpConfigurationModel> configList = jdbcTemplate.query(query, new Object[] { reqModel.getClientId() },
				new RowMapper<FtpConfigurationModel>() {
					@Override
					public FtpConfigurationModel mapRow(ResultSet rs, int rowNum) throws SQLException {
						FtpConfigurationModel model = new FtpConfigurationModel();
						model.setId(rs.getString("id"));
						model.setClientId(rs.getString("client_id"));
						model.setHost(rs.getString("host"));
						model.setPassword(rs.getString("password"));
						model.setPath(rs.getString("path"));
						model.setPort(Integer.parseInt(rs.getString("port")));
						model.setProtocol(rs.getString("protocol"));
						model.setUsername(rs.getString("username"));
						return model;
					}
				});
		return configList;
	}

	@Override
	public GenericResponseModel updateConfiguration(RequestModel reqModel) {
		GenericResponseModel respModel = new GenericResponseModel();
		FtpConfigurationModel incomingConfig = reqModel.getFtpConfig();
		if(incomingConfig.getId() != null && !incomingConfig.getId().isEmpty()) {
			// Existing value update
		String query = "UPDATE ftp_monitoring_config SET host=?, port=?, protocol=?, username=?," + "password=?" + ", path=? WHERE id=?";
		int affected = jdbcTemplate.update(query, new Object[] {
				incomingConfig.getHost(),
				incomingConfig.getPort(),
				incomingConfig.getProtocol(),
				incomingConfig.getUsername(),
				incomingConfig.getPassword(),
				incomingConfig.getPath(),
				incomingConfig.getId()
				});
		
			respModel.setStatus(1);
			respModel.setMessage("Updated " + affected + " row(s) successfully");
		} else {
			// New value. Insert
			String query = "INSERT into ftp_monitoring_config VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			incomingConfig.setId(String.valueOf(UUID.randomUUID()));
			int affected = jdbcTemplate.update(query, new Object[] {
					incomingConfig.getClientId(),
					incomingConfig.getHost(),
					incomingConfig.getPort(),
					incomingConfig.getUsername(),
					incomingConfig.getPassword(),
					incomingConfig.getProtocol(),
					incomingConfig.getPath(),
					incomingConfig.getId()
			});
			respModel.setStatus(1);
			respModel.setMessage("Added " + affected + " record(s) successfully");
		}
		return respModel;
	}
}
