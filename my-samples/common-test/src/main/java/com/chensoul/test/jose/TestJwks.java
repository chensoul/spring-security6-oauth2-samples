package com.chensoul.test.jose;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * @author Joe Grandja
 */
public final class TestJwks {

	private static final KeyPairGenerator rsaKeyPairGenerator;
	static {
		try {
			rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
			rsaKeyPairGenerator.initialize(2048);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	// @formatter:off
	public static final RSAKey DEFAULT_RSA_JWK =
			jwk(
					TestKeys.DEFAULT_PUBLIC_KEY,
					TestKeys.DEFAULT_PRIVATE_KEY
			).build();
	// @formatter:on

	// @formatter:off
	public static final ECKey DEFAULT_EC_JWK =
			jwk(
					(ECPublicKey) TestKeys.DEFAULT_EC_KEY_PAIR.getPublic(),
					(ECPrivateKey) TestKeys.DEFAULT_EC_KEY_PAIR.getPrivate()
			).build();
	// @formatter:on

	// @formatter:off
	public static final OctetSequenceKey DEFAULT_SECRET_JWK =
			jwk(
					TestKeys.DEFAULT_SECRET_KEY
			).build();
	// @formatter:on

	private TestJwks() {
	}

	public static RSAKey.Builder generateRsaJwk() {
		KeyPair keyPair = rsaKeyPairGenerator.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// @formatter:off
		return jwk(publicKey, privateKey)
				.keyID(UUID.randomUUID().toString());
		// @formatter:on
	}

	public static RSAKey.Builder jwk(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
		// @formatter:off
		return new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyUse(KeyUse.SIGNATURE)
				.keyID("rsa-jwk-kid");
		// @formatter:on
	}

	public static ECKey.Builder jwk(ECPublicKey publicKey, ECPrivateKey privateKey) {
		// @formatter:off
		Curve curve = Curve.forECParameterSpec(publicKey.getParams());
		return new ECKey.Builder(curve, publicKey)
				.privateKey(privateKey)
				.keyUse(KeyUse.SIGNATURE)
				.keyID("ec-jwk-kid");
		// @formatter:on
	}

	public static OctetSequenceKey.Builder jwk(SecretKey secretKey) {
		// @formatter:off
		return new OctetSequenceKey.Builder(secretKey)
				.keyUse(KeyUse.SIGNATURE)
				.keyID("secret-jwk-kid");
		// @formatter:on
	}

}