package frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class FrontendSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AppAccessDeniedHandler aDHandler;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable()
         .authorizeRequests()
				.antMatchers("/service/**").hasAnyRole("ADMIN")
				.anyRequest().authenticated()
         .and()
         .formLogin()
				.loginPage("/login")
				.permitAll()
				.defaultSuccessUrl("/service/home")
				.and()
         .logout()
				.permitAll()
				.and()
         .exceptionHandling().accessDeniedHandler(aDHandler);
	}
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
                .withUser("admin").password("Insp!@LogBod12").roles("ADMIN");
    }
}
