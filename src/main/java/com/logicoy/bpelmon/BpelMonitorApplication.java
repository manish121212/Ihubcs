package com.logicoy.bpelmon;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.logicoy.bpelmon.security.LoggingInterceptor;
import com.logicoy.bpelmon.utils.Utils;

@SpringBootApplication
public class BpelMonitorApplication {

	@Autowired
	LoggingInterceptor loggingInterceptor;
	private String OPTIONS = "OPTIONS";
	private String GET = "GET";
	private String POST = "POST";
	private String HEAD = "HEAD";

	public static void main(String[] args) {
		SpringApplication.run(BpelMonitorApplication.class, args);
		System.gc();
	}
	@PostConstruct()
	private void init() {
		this.scheduleGC();
	}
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**").allowedOrigins("*").allowedMethods(GET, POST, HEAD, OPTIONS)
						.allowCredentials(false);
				registry.addMapping("/auth/**").allowedOrigins("*").allowedMethods(GET, POST, HEAD, OPTIONS)
						.allowCredentials(false);
				registry.addMapping("/**").allowedOrigins("*").allowedMethods(GET, POST, HEAD, OPTIONS, "PUT")
						.allowCredentials(false).exposedHeaders(HttpHeaders.CONTENT_DISPOSITION);
				super.addCorsMappings(registry);

			}

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(loggingInterceptor).addPathPatterns("/api/**").excludePathPatterns("/auth/**","/api/settings/**", "/api/reports/**", "/api/**");
				super.addInterceptors(registry);
			}
		};
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};

//		tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
		return tomcat;
	}

	private Connector initiateHttpConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(7070);
		connector.setSecure(false);
		connector.setRedirectPort(8082);

		return connector;
	}
	private void scheduleGC() {
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Garbage collector signalled", "");
				System.gc();
			}
		};
		Timer timer = new Timer("GC Scheduler", true);
		timer.scheduleAtFixedRate(task, 10 * 60 * 1000L, 60 * 60 * 1000L);
	}
}
