package frontend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler {

	private static Logger logger = Logger.getLogger(AppAccessDeniedHandler.class.getName());
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		logger.info("Inside handle");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth != null) {
            logger.info("User '" + auth.getName()
                    + "' attempted to access the protected URL: "
                    + request.getRequestURI());
        }
		logger.info("Context path: " + request.getContextPath());
		response.sendRedirect(request.getContextPath() + "/login");
	}

}
