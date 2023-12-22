package com.vinfast.api;

import com.vinfast.api.common.constants.CommonConst;
import com.vinfast.api.common.constants.TeamcenterConst;
import com.vinfast.api.common.extensions.TCCommonExtension;
import com.vinfast.connect.client.AppXSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;

@ComponentScan({"com.vinfast.api.controller", "com.vinfast.api.service", "com.vinfast.api.event"})
@SpringBootApplication
@EnableAsync
public class ApiApplication {
	private AppXSession session = null;
    public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(ApiApplication.class, args);
    }

	@EventListener(ApplicationReadyEvent.class)
	public void loginTCAfterStart() {
		System.out.println("Hello friend. Start login Teamcenter");
		try {
			session = TCCommonExtension.loginTC(CommonConst.DATAMANAGER_USERNAME, CommonConst.DATAMANAGER_PASSWORD);
//			TCCommonExtension.loadObjectPolicy(TeamcenterConst.OBJECT_PROPERTY_POLICY, AppXSession.getConnection());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void logoutTCBeforeDisconnect() {
		System.out.println("Goodbye friend. Start logout Teamcenter");
		try {
			TCCommonExtension.logoutTC(session, AppXSession.getConnection());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("JDAsync-");
		executor.initialize();
		return executor;
	}
}