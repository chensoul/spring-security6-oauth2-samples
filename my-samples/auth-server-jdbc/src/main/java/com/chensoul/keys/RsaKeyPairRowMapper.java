package com.chensoul.keys;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class RsaKeyPairRowMapper implements RowMapper<RsaKeyPair> {

	private final RsaPrivateKeyConverter rsaPrivateKeyConverter;

	private final RsaPublicKeyConverter rsaPublicKeyConverter;

	RsaKeyPairRowMapper(RsaPrivateKeyConverter rsaPrivateKeyConverter, RsaPublicKeyConverter rsaPublicKeyConverter) {
		this.rsaPrivateKeyConverter = rsaPrivateKeyConverter;
		this.rsaPublicKeyConverter = rsaPublicKeyConverter;
	}

	@Override
	public RsaKeyPair mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			var privateKeyBytes = rs.getString("private_key").getBytes();
			var privateKey = this.rsaPrivateKeyConverter.deserializeFromByteArray(privateKeyBytes);

			var publicKeyBytes = rs.getString("public_key").getBytes();
			var publicKey = this.rsaPublicKeyConverter.deserializeFromByteArray(publicKeyBytes);

			var created = new Date(rs.getDate("created").getTime()).toInstant();
			var id = rs.getString("id");

			return new RsaKeyPair(id, created, publicKey, privateKey);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}