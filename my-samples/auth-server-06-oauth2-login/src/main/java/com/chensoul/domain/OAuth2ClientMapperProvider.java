/**
 * Copyright © 2016-2025 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chensoul.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2ClientMapperProvider {

	@Autowired
	@Qualifier("basicOAuth2ClientMapper")
	private OAuth2ClientMapper basicOAuth2ClientMapper;

	@Autowired
	@Qualifier("customOAuth2ClientMapper")
	private OAuth2ClientMapper customOAuth2ClientMapper;

	@Autowired
	@Qualifier("githubOAuth2ClientMapper")
	private OAuth2ClientMapper githubOAuth2ClientMapper;

	@Autowired
	@Qualifier("appleOAuth2ClientMapper")
	private OAuth2ClientMapper appleOAuth2ClientMapper;

	public OAuth2ClientMapper getOAuth2ClientMapperByType(MapperType oauth2MapperType) {
		switch (oauth2MapperType) {
			case CUSTOM:
				return customOAuth2ClientMapper;
			case BASIC:
				return basicOAuth2ClientMapper;
			case GITHUB:
				return githubOAuth2ClientMapper;
			case APPLE:
				return appleOAuth2ClientMapper;
			default:
				throw new RuntimeException(
						"OAuth2ClientRegistrationMapper with type " + oauth2MapperType + " is not supported!");
		}
	}

}
