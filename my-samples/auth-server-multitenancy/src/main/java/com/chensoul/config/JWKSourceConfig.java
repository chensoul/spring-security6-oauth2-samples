package com.chensoul.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class JWKSourceConfig {

	@Bean
	public JWKSource<SecurityContext> jwkSource(TenantPerIssuerComponentRegistry componentRegistry) {
		componentRegistry.register("issuer1", JWKSet.class, new JWKSet(generateRSAJwk())); // <1>
		componentRegistry.register("issuer2", JWKSet.class, new JWKSet(generateRSAJwk())); // <2>

		return new DelegatingJWKSource(componentRegistry);
	}

	// @fold:on
	private static RSAKey generateRSAJwk() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// @formatter:off
		return new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		// @formatter:on
	}
	// @fold:off

	private static class DelegatingJWKSource implements JWKSource<SecurityContext> {

	// <3>

		private final TenantPerIssuerComponentRegistry componentRegistry;

		private DelegatingJWKSource(TenantPerIssuerComponentRegistry componentRegistry) {
			this.componentRegistry = componentRegistry;
		}

		@Override
		public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
			return jwkSelector.select(getJwkSet());
		}

		private JWKSet getJwkSet() {
			JWKSet jwkSet = this.componentRegistry.get(JWKSet.class); // <4>
			Assert.state(jwkSet != null, "JWKSet not found for \"requested\" issuer identifier."); // <5>
			return jwkSet;
		}

	}

}