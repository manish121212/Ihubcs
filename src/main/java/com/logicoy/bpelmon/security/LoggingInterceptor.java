package com.logicoy.bpelmon.security;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.logicoy.bpelmon.models.Authority;
import com.logicoy.bpelmon.models.UserToken;

/**
 *
 * @author Shrivats
 */
@Controller
public class LoggingInterceptor implements HandlerInterceptor {

	@Autowired
	FetchUserDetails fUDetails;
	// private static Logger logger =
	// Logger.getLogger(LoggingInterceptor.class.getName());

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
			// logger.info("Preflight request. Always return true.");
			return true;
		}

		String authToken = request.getHeader("token");
		String userName = request.getHeader("username");

		if (authToken == null || userName == null) {
			// logger.info("auth token or user name null");
			response.sendError(401);
			return false;
		}

		List<Authority> roles = fUDetails.getUserRole(userName);
		UserToken token = fUDetails.fetchUserToken(userName);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		try {
			if (token.getAuthToken().equals(authToken)
					&& dd.parse(token.getExpiryTime()).getTime() > cal.getTimeInMillis()) {
				// logger.info("User and token valid.");
				boolean isBusinessUser = false;
				boolean isSupportUser = false;
				boolean isAdminUser = false;
				for (Authority role : roles) {
					if (role.getRole().equalsIgnoreCase("INSP_BUSINESS_USER")) {
						isBusinessUser = true;
					}
					if (role.getRole().equalsIgnoreCase("INSP_SUPPORT_USER")) {
						isSupportUser = true;
					}
					if (role.getRole().equalsIgnoreCase("INSP_ADMIN_USER")) {
						isAdminUser = true;
					}
				}
				// logger.info("User roles");
				// logger.info("Business User: " + isBusinessUser);
				// logger.info("Support User: " + isSupportUser);
				// logger.info("Admin User: " + isAdminUser);

				if (isBusinessUser && !isSupportUser && !isAdminUser) {
					if (request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getSelectedUserRoleAuthorities")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getAllUserDetails")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/updateUserRole")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/register")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getUserRoles")) {

						response.sendError(401);
						return false;
					}
				}
				if (isSupportUser && !isBusinessUser && !isAdminUser) {
					if (request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getAllUserDetails")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/updateUserRole")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/register")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getUserRoles")) {

						// response.sendRedirect("/unauthorized");
						response.sendError(401);

						return false;
					}

				}
				if (isBusinessUser && isSupportUser && !isAdminUser) {
					if (request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getAllUserDetails")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/updateUserRole")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/register")
							|| request.getRequestURI().equalsIgnoreCase("/bpelmon/api/getUserRoles")) {

						// response.sendRedirect("/unauthorized");
						response.sendError(401);
						return false;
					}
				}

				return true;
			} else {
				// logger.info("User session not valid");
				long timeDiff = dd.parse(token.getExpiryTime()).getTime() - cal.getTimeInMillis();
				TimeUnit.MINUTES.convert(timeDiff, TimeUnit.MILLISECONDS);
				// logger.info("Time since inactive: " + mins + " mins");
				response.sendError(401);
				return false;
			}
		} catch (Exception e) {
			response.sendError(401);
			return false;
		}
	}

}