package com.logicoy.bpelmon.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.logicoy.bpelmon.DAO.UserRegistration;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.LoginRequstModel;
import com.logicoy.bpelmon.models.LoginResponseModel;
import com.logicoy.bpelmon.models.UserDTO;
import com.logicoy.bpelmon.security.FetchUserDetails;
import com.logicoy.bpelmon.security.InvalidLoginException;
import com.logicoy.bpelmon.services.ConnectionManager;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.ElasticClientIndexUpdater;
import com.logicoy.bpelmon.utils.ElasticClientIndexUpdater.OnIndexUpdateComplete;

@RestController
@RequestMapping("/auth")
public class LoginController implements OnIndexUpdateComplete {

	@Autowired
	FetchUserDetails fUDetails;
	@Autowired
	UserRegistration user;
	@Autowired
	AppConstants appConst;
	@Autowired
	ConnectionManager connectionManager;

	private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

	@RequestMapping(value = "/login1", method = RequestMethod.GET)
	public ModelAndView login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, HttpServletRequest request) {

		LOGGER.info("LoginController called");

		ModelAndView model = new ModelAndView();
		if (error != null) {
			model.addObject("error", getErrorMessage(request, "SPRING_SECURITY_LAST_EXCEPTION"));
		}

		if (logout != null) {
			model.addObject("msg", "You've been logged out successfully.");
		}

		model.setViewName("login");

		return model;

	}

	// customize the error message
	private String getErrorMessage(HttpServletRequest request, String key) {

		Exception exception = (Exception) request.getSession().getAttribute(key);

		String error = "";
		if (exception instanceof BadCredentialsException) {
			error = "Invalid username and password!";
		} else if (exception instanceof LockedException) {
			error = exception.getMessage();
		} else if (exception instanceof InvalidLoginException) {
			error = exception.getMessage();
		} else {
			error = "Invalid username and password ! " + exception.getMessage();
		}

		LOGGER.info("Inside Main controller, erro : " + error);
		return error;
	}

	// for 403 access denied page
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public ModelAndView accesssDenied() {

		ModelAndView model = new ModelAndView();

		// check if user is logged in
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			UserDetails userDetail = (UserDetails) auth.getPrincipal();
			LOGGER.info("/403 page" + userDetail);

			model.addObject("username", userDetail.getUsername());

		}

		model.setViewName("403");
		return model;

	}

	@PostMapping("/login")
	public LoginResponseModel authenticate(@RequestBody LoginRequstModel body) {
		LOGGER.info("login endpoint");
		LoginResponseModel responseModel = new LoginResponseModel();
		responseModel = fUDetails.authenticate(body.getUserName(), body.getPassword());
		if (responseModel != null) {
			// Set Field data to corresponding client Id
			try {
				ElasticClientIndexUpdater updater = new ElasticClientIndexUpdater(
						responseModel.getUserdetails().getClientName().toLowerCase().trim(),
						appConst.getPort(),
						appConst.getHost(),
						appConst.getProtocol(),
						this);
				Thread t = new Thread(updater);
				t.setName("ElasticIndexUpdater");
				t.start();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				connectionManager.addClientConnection(responseModel.getUserdetails().getClientName().toLowerCase());
			} catch (NullPointerException ex) {
				LOGGER.severe(ex.getMessage() + " Caused by: " + ex.getCause());
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				LOGGER.severe(e.getMessage());
				Thread.currentThread().interrupt();
			}
			// Create a connection for client
			
			return responseModel;
		} else {
			responseModel = new LoginResponseModel();
			responseModel.setStatus(0);
			responseModel.setMessage("Invalid username or password");
			responseModel.setExpiryTime("");
			responseModel.setToken("");
			return responseModel;
		}
	}

	@PostMapping("/logout")
	public LoginResponseModel logOut(@RequestBody String username) {
		LoginResponseModel responseModel = new LoginResponseModel();
		boolean loggedOut = fUDetails.LogOut(username);
		if (loggedOut == true) {
			responseModel.setStatus(0);
			responseModel.setMessage("Logged out successfully");
			responseModel.setExpiryTime("");
			responseModel.setToken("");
			responseModel.setAuthority(null);
			responseModel.setUserdetails(new UserDTO());
			;

		}
		return responseModel;
	}

	@GetMapping("/recoverPassword")
	public GenericResponseModel recoverPassword(String email) {
		return user.recoverPassword(email.trim());
	}

	@GetMapping("/testProperty")
	public String testProperty() {
		return appConst.getTestProperty();
	}

	@Override
	public void onIndexUpdate(boolean status) {
		LOGGER.log(Level.INFO, "Index updation complete with status {0}", new Object[] {status});
	}
}
