package com.chensoul.test.junit;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.BeanIds;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.web.GenericXmlWebContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rob Winch
 */
public class SpringTestContext implements Closeable {

	private Object test;

	private ConfigurableWebApplicationContext context;

	private List<Filter> filters = new ArrayList<>();

	public SpringTestContext(Object test) {
		setTest(test);
	}

	public void setTest(Object test) {
		this.test = test;
	}

	@Override
	public void close() {
		try {
			this.context.close();
		}
		catch (Exception ex) {
		}
	}

	public SpringTestContext context(ConfigurableWebApplicationContext context) {
		this.context = context;
		return this;
	}

	public SpringTestContext register(Class<?>... classes) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(classes);
		this.context = applicationContext;
		return this;
	}

	public SpringTestContext testConfigLocations(String... configLocations) {
		GenericXmlWebContextLoader loader = new GenericXmlWebContextLoader();
		String[] locations = loader.processLocations(this.test.getClass(), configLocations);
		return configLocations(locations);
	}

	public SpringTestContext configLocations(String... configLocations) {
		XmlWebApplicationContext context = new XmlWebApplicationContext();
		context.setConfigLocations(configLocations);
		this.context = context;
		return this;
	}

	public SpringTestContext mockMvcAfterSpringSecurityOk() {
		return addFilter(new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) {
				response.setStatus(HttpServletResponse.SC_OK);
			}
		});
	}

	private SpringTestContext addFilter(Filter filter) {
		this.filters.add(filter);
		return this;
	}

	public ConfigurableWebApplicationContext getContext() {
		if (!this.context.isRunning()) {
			this.context.setServletContext(new MockServletContext());
			this.context.setServletConfig(new MockServletConfig());
			this.context.refresh();
		}
		return this.context;
	}

	public void autowire() {
		this.context.setServletContext(new MockServletContext());
		this.context.setServletConfig(new MockServletConfig());
		this.context.refresh();
		if (this.context.containsBean(BeanIds.SPRING_SECURITY_FILTER_CHAIN)) {
			// @formatter:off
			MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.context).
					apply(SecurityMockMvcConfigurers.springSecurity())
					.apply(new AddFilter())
					.build();
			// @formatter:on
			this.context.getBeanFactory().registerResolvableDependency(MockMvc.class, mockMvc);
		}
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(this.context.getBeanFactory());
		bpp.processInjection(this.test);
	}

	private class AddFilter implements MockMvcConfigurer {

		@Override
		public RequestPostProcessor beforeMockMvcCreated(ConfigurableMockMvcBuilder<?> builder,
				WebApplicationContext context) {
			builder.addFilters(SpringTestContext.this.filters.toArray(new Filter[0]));
			return null;
		}

	}

}