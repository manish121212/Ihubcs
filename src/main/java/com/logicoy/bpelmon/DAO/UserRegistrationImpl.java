package com.logicoy.bpelmon.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import com.logicoy.bpelmon.models.Authority;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.UserAuthority;
import com.logicoy.bpelmon.models.UserDTO;
import com.logicoy.bpelmon.models.UserDetails;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.Email;
import com.logicoy.bpelmon.utils.EnvPropertyReader;
import com.logicoy.bpelmon.utils.PasswordUtil;

@Repository
public class UserRegistrationImpl extends JdbcDaoSupport implements UserRegistration {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Email email;
	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	PasswordUtil passwordUtil;
	@Autowired
	AppConstants appConstants;

	private Logger LOGGER = Logger.getLogger(this.getClass().getName());

	public int registerUser(UserDetails userDetails) {
		LOGGER.info("User role is " + userDetails.getUserrole());
		try {
			// checking whether the user is already exist in table
			String checkUser = "select * from user;";
			List<Map<String, Object>> username = jdbcTemplate.queryForList(checkUser);
			System.out.println("fetching the user list from user table");
			for (Map<String, Object> uniqueUserName : username) {
				if (uniqueUserName.get("username").equals(userDetails.getUsername())) {
					System.out.println("the user is already exist in the table");
					return 3; // 3 for username already exist
				} else if (uniqueUserName.get("email").equals(userDetails.getEmail())) {
					System.out.println("the email is already exist in table");
					return 4; // 4 for email already exist for user
				}
			}
			String initialPassword = passwordUtil.getInitialPassword();
			String passwordEncrypted = passwordUtil.getBCryptPassword(initialPassword);

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			String date_s = userDetails.getUserExpiryTime() + " 23:59:59";

			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = dt.parse(date_s);

			SimpleDateFormat dt1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			// System.out.println("the formated date " + dt1.format(date));
			System.out.println("the password is " + initialPassword);

			// saving user data in user table
			String sql = "insert into user(username,password,firstname,lastname,email,registration_date,expired_date) values(?,?,?,?,?,?,?);";
			int isUserUpdated = jdbcTemplate.update(sql,
					new Object[] { userDetails.getUsername(), passwordEncrypted, userDetails.getFirstName(),
							userDetails.getLastName(), userDetails.getEmail(), dd.format(cal.getTime()),
							dt1.format(date) });

			// removing and adding authorities for the user
			String deleteAllAuthority = "delete from user_authority where user_Id =(select Id from user where username = ?)";
			try {
				int isUserMapDeleted = jdbcTemplate.update(deleteAllAuthority,
						new Object[] { userDetails.getUsername() });
				if (isUserMapDeleted > 0) {
					logger.info("the user previous authorities are deleted");
				}
			} catch (Exception e) {
				LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			}

			int isCompanyMapUpdated = 0;

			String deleteCompanyMapWithUserr = "delete from user_company_mapping where user_Id =(select Id from user where username = ?)";
			try {
				jdbcTemplate.update(deleteCompanyMapWithUserr, new Object[] { userDetails.getUsername() });
			} catch (Exception ee) {
				LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
			}
			// saving company details with user
			String addCompanyMapWithUser = "insert into user_company_mapping(user_Id, company_Id) values ((select Id from user where username  = ?), (select Id from company_details where company_name = ?))";
			try {
				isCompanyMapUpdated = jdbcTemplate.update(addCompanyMapWithUser,
						new Object[] { userDetails.getUsername(), userDetails.getCompany() });
			} catch (Exception ee) {
				LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
			}

			int isAllRoleUpdated = 0;
			for (Authority userAuthority : userDetails.getUserrole()) {
				String addAuthoritiesForUser = "insert into user_authority(user_Id, authority_Id) values ((select Id from user where username  = ?), (select Id from authority where role = ?))";
				try {
					int isRoleUpdated = jdbcTemplate.update(addAuthoritiesForUser,
							new Object[] { userDetails.getUsername(), userAuthority.getRole() });
					if (isRoleUpdated > 0) {
						isAllRoleUpdated += isRoleUpdated;
					}
				} catch (Exception ee) {
					LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
				}
			}

			if (isUserUpdated > 0 && isCompanyMapUpdated > 0 && isAllRoleUpdated > 0) {
				String mailBody = "Hi " + userDetails.getFirstName()
						+ ",<br /><br /> You account has been created with iHubCS. You can change your password on your next login.<br/><br/><br/>User ID : "
						+ userDetails.getUsername() + "<br/><br />Password : " + initialPassword
						+ "<br /><br /> Instance URL : " + appConstants.getInstanceUrl()
						+ " <br /><br /><br />Thank you,<br/>iHubCS Team";

				System.out.println("the user is updated in the db");

				email.sendMail(userDetails.getEmail(),
						appConstants.getRegistrationMailSubject(), mailBody);
				return isUserUpdated;
			}
			return 0;
		} catch (Exception e) {
			System.out.println("failed to update new user");
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return 2;// if any error ralated to the jdbc connection
		}
	}

	public List<UserDetails> getAllUserDetails() {

		String getAllUsers = "select a.username, a.firstname,a.lastname,a.email,a.expired_date, b.company_name from user a, company_details b, user_company_mapping c where a.Id = c.user_Id and c.company_Id = b.Id order by username";
		try {
			List<UserDetails> allUserDetails = jdbcTemplate.query(getAllUsers, new RowMapper<UserDetails>() {
				@Override
				public UserDetails mapRow(ResultSet rs, int i) throws SQLException {
					UserDetails user = new UserDetails();
					user.setUsername(rs.getString("username"));
					user.setFirstName(rs.getString("firstname"));
					user.setLastName(rs.getString("lastname"));
					user.setEmail(rs.getString("email"));
					user.setUserExpiryTime(rs.getString("expired_date"));
					user.setCompany(rs.getString("company_name"));
					return user;
				}
			});
			System.out.println("the user details " + allUserDetails.size());
			return allUserDetails;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return null;
		}
	}

	public List<Authority> getAllUserRoles() {
		String getAllRoles = "select * from authority";
		try {
			List<Authority> allRoles = jdbcTemplate.query(getAllRoles, new RowMapper<Authority>() {
				@Override
				public Authority mapRow(ResultSet rs, int i) throws SQLException {
					Authority userRole = new Authority();
					userRole.setId(rs.getInt("Id"));
					userRole.setRole(rs.getString("role"));
					userRole.setDisplayName(rs.getString("display_name"));
					return userRole;
				}
			});
			System.out.println("the user roles " + allRoles.size());
			return allRoles;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return null;
		}
	}

	public List<Authority> getSelectedUserRoles(String username) {
		String getAllRoles = "select * from authority a, user b, user_authority c where a.Id = c.authority_Id and c.user_Id = b.Id and b.username =?;";
		try {
			List<Authority> allRoles = jdbcTemplate.query(getAllRoles, new Object[] { username },
					(ResultSet rs, int i) -> {
						Authority roles = new Authority();
						roles.setId(rs.getInt("id"));
						roles.setRole(rs.getString("role"));
						roles.setDisplayName(rs.getString("display_name"));
						return roles;
					});
			System.out.println("the user roles " + allRoles.size());
			return allRoles;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return null;
		}
	}

	public int updateUserRoles(UserAuthority userAuthorities) {
		System.out.println("in the user authorities " + userAuthorities.getUserName());
		String deleteAllAuthority = "delete from user_authority where user_Id =(select Id from user where username = ?)";
		try {
			int isDeleted = jdbcTemplate.update(deleteAllAuthority, new Object[] { userAuthorities.getUserName() });
			if (isDeleted > 0) {
				logger.info("the user previous authorities are deleted");
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}

		int isUpdated = 0;
		// for (Authority authty : userAuthorities.getAuthority()) {
		String getAllRoles = "insert into user_authority(user_Id, authority_Id) values ((select Id from user where username  = ?), (select Id from authority where role = ?))";
		try {
			isUpdated = jdbcTemplate.update(getAllRoles,
					new Object[] { userAuthorities.getUserName(), userAuthorities.getUserRole() });
		} catch (Exception ee) {
			LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
		}
		// }
		return isUpdated;
	}

	@Override
	public List<UserDetails> searchAllUserDetailsByUsername(UserDetails details) {
		System.out.println("the user query for user name  " + details.getUsername());
		String getAllUsers = "select * from user where username like %?%";
		try {
			List<UserDetails> allUserDetails = jdbcTemplate.query(getAllUsers, new Object[] { details.getUsername() },
					new RowMapper<UserDetails>() {
						@Override
						public UserDetails mapRow(ResultSet rs, int i) throws SQLException {
							UserDetails user = new UserDetails();
							user.setUsername(rs.getString("username"));
							user.setFirstName(rs.getString("firstname"));
							user.setLastName(rs.getString("lastname"));
							user.setEmail(rs.getString("email"));
							user.setUserExpiryTime(rs.getString("expired_date"));
							return user;
						}
					});
			System.out.println("the user details in the search query " + allUserDetails.size());
			return allUserDetails;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return null;
		}
	}

	@Override
	public int updateExistingUser(UserDetails userDetails) {
		try {
			// checking whether user already exists
			String checkUser = "select * from user where username !='" + userDetails.getUsername() + "\'";
			List<Map<String, Object>> userdata = jdbcTemplate.queryForList(checkUser);
			System.out.println("fetching the user list from user table");
			for (Map<String, Object> uniqueUserName : userdata) {
				if (uniqueUserName.get("email").equals(userDetails.getEmail())) {
					System.out.println("the email is already exist in table");
					return 4; // 4 for email already exist for user
				}
			}

			String date_s = userDetails.getUserExpiryTime() + " 23:59:59";

			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = dt.parse(date_s);

			SimpleDateFormat dt1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			String sql = "update user set firstname = ?,lastname = ?,email =? ,expired_date =? where username =? ";
			jdbcTemplate.update(sql, new Object[] { userDetails.getFirstName(), userDetails.getLastName(),
					userDetails.getEmail(), dt1.format(date), userDetails.getUsername() });

			// removing and adding authorities for the user
			String deleteAllAuthority = "delete from user_authority where user_Id =(select Id from user where username = ?)";
			try {
				int isUserMapDeleted = jdbcTemplate.update(deleteAllAuthority,
						new Object[] { userDetails.getUsername() });
				if (isUserMapDeleted > 0) {
					logger.info("the user previous authorities are deleted");
				}
			} catch (Exception e) {
				LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			}

			String deleteCompanyMapWithUserr = "delete from user_company_mapping where user_Id =(select Id from user where username = ?)";
			try {
				jdbcTemplate.update(deleteCompanyMapWithUserr, new Object[] { userDetails.getUsername() });
			} catch (Exception e) {
				LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			}
			try {
			} catch (Exception ee) {
				LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
				return 2;
			}

			for (Authority userAuthority : userDetails.getUserrole()) {
				String addAuthoritiesForUser = "insert into user_authority(user_Id, authority_Id) values ((select Id from user where username  = ?), (select Id from authority where role = ?))";
				try {
					int isRoleUpdated = jdbcTemplate.update(addAuthoritiesForUser,
							new Object[] { userDetails.getUsername(), userAuthority.getRole() });
					if (isRoleUpdated > 0) {
					}
				} catch (Exception ee) {
					LOGGER.severe(ee.getMessage() + " Caused by: " + ee.getCause());
					return 2;
				}
			}
			// Update user company mapping
			String addCompanyMapWithUser = "insert into user_company_mapping(user_Id, company_Id) values ((select Id from user where username  = ?), (select Id from company_details where company_name = ?))";
			try {
				int isCompanyMapUpdated = jdbcTemplate.update(addCompanyMapWithUser,
						new Object[] { userDetails.getUsername(), userDetails.getCompany() });
				LOGGER.log(Level.INFO, "User {} company mapping updated to {} with status {}", new Object[] {userDetails.getUsername(), userDetails.getCompany(), isCompanyMapUpdated});
			} catch (Exception ee) {
				LOGGER.log(Level.SEVERE, "Error while updating user mapping", ee);
				return 2;
			}

			return 1;
		} catch (Exception e) {
			System.out.println("failed to update new user");
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
			return 2;// if any error ralated to the jdbc connection
		}
	}

	@Override
	public List<UserDetails> getUserDetailsByUserName(UserDetails details) {
		String getUserDetailsByUserName = "select * from user where username like '%?%';";
		try {
			List<UserDetails> userDetails = jdbcTemplate.query(getUserDetailsByUserName,
					new Object[] { details.getUsername() }, new RowMapper<UserDetails>() {
						@Override
						public UserDetails mapRow(ResultSet rs, int i) throws SQLException {
							UserDetails uDetails = new UserDetails();
							uDetails.setUsername(rs.getString("username"));
							uDetails.setFirstName(rs.getString("firstname"));
							uDetails.setLastName(rs.getString("lastname"));
							uDetails.setUserExpiryTime(rs.getString("expired_date"));
							uDetails.setEmail(rs.getString("email"));
							return uDetails;
						}
					});
			return userDetails;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}
		return null;
	}

	public UserDetails getUserDetailsByEmail(String email) {
		String getUserDetailsByUserName = "select * from user where email = ?";
		try {
			List<UserDetails> userDetails = jdbcTemplate.query(getUserDetailsByUserName, new Object[] { email },
					new RowMapper<UserDetails>() {
						@Override
						public UserDetails mapRow(ResultSet rs, int i) throws SQLException {
							UserDetails uDetails = new UserDetails();
							uDetails.setUsername(rs.getString("username"));
							uDetails.setFirstName(rs.getString("firstname"));
							uDetails.setLastName(rs.getString("lastname"));
							uDetails.setUserExpiryTime(rs.getString("expired_date"));
							uDetails.setEmail(rs.getString("email"));
							return uDetails;
						}
					});

			if (userDetails != null && userDetails.size() > 0) {
				return userDetails.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}
		return null;
	}

	@Override
	public GenericResponseModel recoverPassword(String recoveryEmail) {
		logger.info("Recovery Email: " + recoveryEmail);
		GenericResponseModel response = new GenericResponseModel();

		String initialPassword = passwordUtil.getInitialPassword();
		String passwordEncrypted = passwordUtil.getBCryptPassword(initialPassword);

		logger.info("Generated password is: " + initialPassword);
		logger.info("Encrypted password is: " + passwordEncrypted);

		// saving user data in user table
		String sql = "update user set " + "password=?" +" where email=?;";
		int isUserUpdated = jdbcTemplate.update(sql, new Object[] { passwordEncrypted, recoveryEmail.trim() });

		logger.info("Affected rows: " + isUserUpdated);

		if (isUserUpdated > 0) {
			// Send recovery email
			logger.info("Valid email. Sending recovery mail");

			UserDetails otherUserDetails = getUserDetailsByEmail(recoveryEmail);

			String mailBody = "Hi " + otherUserDetails.getFirstName()
					+ ",<br /><br /> Please find your new autogenerated password below.<br/><br/><br/>Username : "
					+ otherUserDetails.getUsername() + "<br/><br/>Password : " + initialPassword
					+ "<br /><br /> Instance URL : " + appConstants.getInstanceUrl()
					+ " <br /><br /><br />Thank you,<br/>iHubCS Team";

			email.sendMail(recoveryEmail.trim(), "Recovered password to access iHubCS monitoring interface", mailBody);
			response.setStatus(1);
			response.setMessage("Password reset. Please check your email.");
		} else {
			response.setMessage("Email is not associated with any user account");
			response.setStatus(0);
		}
		return response;
	}

	@Override
	public GenericResponseModel changeUserPassword(String newPassword, UserDTO details) {
		GenericResponseModel response = new GenericResponseModel();

		String passwordEncrypted = passwordUtil.getBCryptPassword(newPassword);

		// saving user data in user table
		String sql = "update user set " + "password=?" + " where username=?;";
		int isUserUpdated = jdbcTemplate.update(sql, new Object[] { passwordEncrypted, details.getUsername() });

		if (isUserUpdated > 0) {
			// Send recovery email
			logger.info("Valid email. Sending password change mail");

			String mailBody = "Hi " + details.getFirstName()
					+ ",<br/><br/>You have successfully updated your password.<br/><br/>"
					+ "<br/><br/>Thank you,<br/>iHubCS Team";

			email.sendMail(
					details.getEmail().trim(), "Your updated password to access monitoring interface", mailBody);
			response.setStatus(1);
			response.setMessage("Password reset. Please check your email.");
		} else {
			response.setMessage("Can not update password. Invalid user session found");
			response.setStatus(0);
		}
		return response;
	}

	@Override
	public GenericResponseModel updateUserProfile(UserDTO details) {
		GenericResponseModel model = new GenericResponseModel();
		model.setStatus(0);
		model.setMessage("Error processing request. Please try after some time.");

		logger.info("updating user details..");
		String sql = "update user set firstname = ?,lastname = ?,email =? where username =? ";
		int isUserUpdated = jdbcTemplate.update(sql, new Object[] { details.getFirstName(), details.getLastName(),
				details.getEmail(), details.getUsername() });
		logger.info("Updated rows: " + isUserUpdated);
		if (isUserUpdated > 0) {
			model.setStatus(1);
			model.setMessage("Profile details successfully updated.");
		}
		return model;
	}

}
