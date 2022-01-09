package com.logicoy.bpelmon.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.logicoy.bpelmon.models.Authority;
import com.logicoy.bpelmon.models.LoginResponseModel;
import com.logicoy.bpelmon.models.UserDTO;
import com.logicoy.bpelmon.models.UserToken;

/**
 *
 * @author Shrivats
 */
@Service
public class FetchUserDetails extends JdbcDaoSupport {

	// private static final Logger logger = Logger.getLogger("FetchUserDetails");
	private LoginResponseModel model;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	private Logger LOGGER = Logger.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public UserToken fetchUserToken(String username) {
		// logger.info("Fetching user token");
		UserToken tokenDetails = jdbcTemplate.queryForObject("select * from user_token_manage where username = ?",
				new Object[] { username }, (ResultSet rs, int i) -> {
					UserToken token = new UserToken();
					token.setUserName(rs.getString("username"));
					token.setAuthToken(rs.getString("auth_token"));
					token.setExpiryTime(rs.getString("expiry_time"));
					return token;
				});
		// logger.info("User token details" + tokenDetails.toString());
		return tokenDetails;
	}

	public List<Authority> getUserRole(String username) {
		// logger.info("Fetching roles");
		List<Authority> userAuthorities = jdbcTemplate.query(
				"select * from authority where Id in (select authority_Id from user_authority where user_Id = (select Id from user where username = ?))",
				new Object[] { username }, (ResultSet rs, int i) -> {
					Authority roles = new Authority();
					roles.setId(rs.getInt("id"));
					roles.setRole(rs.getString("role"));
					return roles;
				});
		// logger.info("Roles for " + username + " are: " + userAuthorities.toString());
		return userAuthorities;
	}

	public LoginResponseModel authenticate(String username, String password) {
		UserDTO userDTO;
		List<Authority> userAuthorities = null;
		// logger.info("Authenticating " + username + ".");
		model = new LoginResponseModel();
		try {
			userDTO = jdbcTemplate.queryForObject(
					"select a.username,a.password, a.firstname,a.lastname,a.email,a.expired_date, b.company_name, b.Id, b.timezone, b.payload_status from user a, company_details b, user_company_mapping c where a.Id = c.user_Id and c.company_Id = b.Id and a.username = ?",
					new Object[] { username }, (ResultSet rs, int i) -> {
						UserDTO user = new UserDTO();
						// user.setId(rs.getInt("id"));
						user.setPassword(rs.getString("password"));
						user.setUsername(rs.getString("username"));
						user.setEmail(rs.getString("email"));
						user.setFirstName(rs.getString("firstname"));
						user.setLastName(rs.getString("lastname"));
						user.setExpiryDate(rs.getString("expired_date"));
						user.setClientName(rs.getString("company_name"));
						user.setTimezone(rs.getString("timezone"));
						user.setPayloadStatus(rs.getInt("payload_status"));
						user.setCompanyId(rs.getInt("b.Id"));
						return user;
					});

			// logger.info("User " + username + " exists. Checking password...");

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			if (!encoder.matches(password, userDTO.getPassword())) {
				logger.info("User password doesn't match");
				model.setStatus(0);
				model.setMessage("Invalid username or password");
				model.setExpiryTime("");
				model.setToken("");
				return model;
			}
			// logger.info("User authenticated with password. Checking expiry...");
			userDTO.setPassword(null); // setting password null

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			if (dd.parse(userDTO.getExpiryDate()).getTime() > cal.getTimeInMillis()) {
				logger.info("User account active. Checking roles...");
				try {
					if (userDTO != null) {
						userAuthorities = jdbcTemplate.query(
								"select * from authority where Id in (select authority_Id from user_authority where user_Id = (select Id from user where username = ?))",
								new Object[] { username }, (ResultSet rs, int i) -> {
									Authority roles = new Authority();
									roles.setId(rs.getInt("id"));
									roles.setRole(rs.getString("role"));
									roles.setDisplayName(rs.getString("display_name"));
									return roles;
								});

						if (userAuthorities.size() > 0) {
							// logger.info("User has following roles");
							// logger.info(userAuthorities.toString());

							String name = username + dd.format(cal.getTime());
							String authToken = Base64Utils.encodeToUrlSafeString(name.getBytes());
							// logger.info("Generated user token. Checking whether user exists in
							// user_token_manage");
							try {
								List<UserToken> userExist = jdbcTemplate.query("select * from user_token_manage;",
										new RowMapper<UserToken>() {
											@Override
											public UserToken mapRow(ResultSet rs, int i) throws SQLException {
												UserToken roles = new UserToken();
												roles.setUserName(rs.getString("username"));
												roles.setAuthToken(rs.getString("auth_token"));
												roles.setExpiryTime(rs.getString("expiry_time"));
												return roles;
											}
										});

								cal.add(Calendar.HOUR, 1);

								if (containsName(userExist, username)) {
									
									jdbcTemplate.update(
											"update user_token_manage set auth_token = ?,expiry_time = ? where username = ?;",
											new Object[] { authToken, dd.format(cal.getTime()), username });
								} else {
									jdbcTemplate.update(
											"insert into user_token_manage(username,auth_token,expiry_time) values(?,?,?);",
											new Object[] { username, authToken, dd.format(cal.getTime()) });
								}
								// System.out.println("in the end of authentication");
								model.setStatus(1);
								model.setMessage("loggedin successfully");
								model.setExpiryTime(dd.format(cal.getTime()));
								model.setToken(authToken);
								model.setUserdetails(userDTO);
								model.setAuthority(userAuthorities);
								
							} catch (Exception e) {
								logger.error("Exception/Info: " + e.getMessage() + "caused by: " + e.getCause());
							}
						} else {
							// System.out.println("here ");
							model.setStatus(2);
							model.setMessage("Unauthorized role");
							model.setExpiryTime("");
							model.setToken("");
							model.setUserdetails(null);
							model.setAuthority(null);
						}

					}
				} catch (Exception eex) {
					logger.error("Exception/Info: " + eex.getMessage() + "caused by: " + eex.getCause());
				}
			} else {
				model.setStatus(3);
				model.setMessage("Your account has been expired");
				model.setExpiryTime("");
				model.setToken("");
				model.setUserdetails(null);
				model.setAuthority(null);
			}
			return model;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			// System.out.println("Query Exception");
			model.setStatus(0);
			model.setMessage("Invalid username or password");
			model.setExpiryTime("");
			model.setToken("");
			return model;
		}
	}

	public boolean containsName(final List<UserToken> list, final String username) {
		return list.stream().filter(o -> o.getUserName().equalsIgnoreCase(username)).findFirst().isPresent();
	}

	public boolean LogOut(String username) {
		try {
			int affected = jdbcTemplate.update("update user_token_manage set auth_token = null where username = ?;",
					new Object[] { username });
			if (affected > 0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return false;
	}
}