/*
 * Copyright 2014 Tibor Bombiak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Based on JdbcTokenStore by Ken Dombeck, Luke Taylor and Dave Syer.
 */

package com.github.biegleux.gae.oauth.tokenstore;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.github.biegleux.gae.oauth.tokenstore.persistence.GaeOAuthAccessTokenRepository;
import com.github.biegleux.gae.oauth.tokenstore.persistence.GaeOAuthRefreshTokenRepository;
import com.github.biegleux.gae.oauth.tokenstore.persistence.model.GaeOAuthAccessToken;
import com.github.biegleux.gae.oauth.tokenstore.persistence.model.GaeOAuthRefreshToken;

/**
 * Implementation of token services that stores OAuth tokens in GAE Datastore.
 */
public class GaeTokenStore implements TokenStore {

	private static final Log LOG = LogFactory.getLog(GaeTokenStore.class);

	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	private final GaeOAuthAccessTokenRepository accessTokens;
	private final GaeOAuthRefreshTokenRepository refreshTokens;

	public GaeTokenStore() {
		accessTokens = new GaeOAuthAccessTokenRepository();
		refreshTokens = new GaeOAuthRefreshTokenRepository();
	}

	public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
		this.authenticationKeyGenerator = authenticationKeyGenerator;
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		OAuth2AccessToken accessToken = null;

		String key = authenticationKeyGenerator.extractKey(authentication);
		try {
			GaeOAuthAccessToken gaeOAuthAccessToken = accessTokens.findByAuthenticationId(key);
			if (gaeOAuthAccessToken != null) {
				accessToken = gaeOAuthAccessToken.getToken();
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Failed to find access token for authentication " + authentication);
				}
			}
		} catch (JDOException e) {
			LOG.error("Could not extract access token for authentication " + authentication, e);
		}

		if (accessToken != null && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
			removeAccessToken(accessToken.getValue());
			// Keep the store consistent (maybe the same user is represented by this authentication but the details have changed)
			storeAccessToken(accessToken, authentication);
		}
		return accessToken;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}

		if (readAccessToken(token.getValue()) != null) {
			removeAccessToken(token.getValue());
		}

		GaeOAuthAccessToken gaeOAuthAccessToken = new GaeOAuthAccessToken();
		gaeOAuthAccessToken.setTokenId(extractTokenKey(token.getValue()));
		gaeOAuthAccessToken.setToken(token);
		gaeOAuthAccessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
		gaeOAuthAccessToken.setUsername(authentication.isClientOnly() ? null : authentication.getName());
		gaeOAuthAccessToken.setClientId(authentication.getOAuth2Request().getClientId());
		gaeOAuthAccessToken.setAuthentication(authentication);
		gaeOAuthAccessToken.setRefreshToken(extractTokenKey(refreshToken));
		accessTokens.save(gaeOAuthAccessToken);
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		OAuth2AccessToken accessToken = null;

		try {
			GaeOAuthAccessToken gaeOAuthAccessToken = accessTokens.findByTokenId(extractTokenKey(tokenValue));
			if (gaeOAuthAccessToken != null) {
				accessToken = gaeOAuthAccessToken.getToken();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to find access token for token " + tokenValue);
				}
			}
		} catch (JDOException e) {
			LOG.warn("Failed to deserialize access token for " + tokenValue, e);
			removeAccessToken(tokenValue);
		}

		return accessToken;
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		removeAccessToken(token.getValue());
	}

	public void removeAccessToken(String tokenValue) {
		accessTokens.deleteByTokenId(extractTokenKey(tokenValue));
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		OAuth2Authentication authentication = null;

		try {
			GaeOAuthAccessToken gaeOAuthAccessToken = accessTokens.findByTokenId(extractTokenKey(token));
			if (gaeOAuthAccessToken != null) {
				authentication = gaeOAuthAccessToken.getAuthentication();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to find access token for token " + token);
				}
			}
		} catch (JDOException e) {
			LOG.warn("Failed to deserialize authentication for " + token, e);
			removeAccessToken(token);
		}

		return authentication;
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		GaeOAuthRefreshToken gaeOAuthRefreshToken = new GaeOAuthRefreshToken();
		gaeOAuthRefreshToken.setTokenId(extractTokenKey(refreshToken.getValue()));
		gaeOAuthRefreshToken.setToken(refreshToken);
		gaeOAuthRefreshToken.setAuthentication(authentication);
		refreshTokens.save(gaeOAuthRefreshToken);
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		OAuth2RefreshToken refreshToken = null;

		try {
			GaeOAuthRefreshToken gaeOAuthRefreshToken = refreshTokens.findByTokenId(extractTokenKey(tokenValue));
			if (gaeOAuthRefreshToken != null) {
				refreshToken = gaeOAuthRefreshToken.getToken();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to find refresh token for token " + tokenValue);
				}
			}
		} catch (JDOException e) {
			LOG.warn("Failed to deserialize refresh token for token " + tokenValue, e);
			removeRefreshToken(tokenValue);
		}

		return refreshToken;
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		removeRefreshToken(token.getValue());
	}

	public void removeRefreshToken(String token) {
		refreshTokens.deleteByTokenId(extractTokenKey(token));
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		return readAuthenticationForRefreshToken(token.getValue());
	}

	public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
		OAuth2Authentication authentication = null;

		try {
			GaeOAuthRefreshToken gaeOAuthRefreshToken = refreshTokens.findByTokenId(extractTokenKey(value));
			if (gaeOAuthRefreshToken != null) {
				authentication = gaeOAuthRefreshToken.getAuthentication();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Failed to find access token for token " + value);
				}
			}
		} catch (JDOException e) {
			LOG.warn("Failed to deserialize access token for " + value, e);
			removeRefreshToken(value);
		}

		return authentication;
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
	}

	public void removeAccessTokenUsingRefreshToken(String refreshToken) {
		accessTokens.deleteByRefreshToken(extractTokenKey(refreshToken));
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();

		List<GaeOAuthAccessToken> gaeOAuthAccessTokens = this.accessTokens.findByClientId(clientId);

		if (gaeOAuthAccessTokens.isEmpty()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find access token for clientId " + clientId);
			}
		}

		for (GaeOAuthAccessToken gaeOAuthAccessToken : gaeOAuthAccessTokens) {
			try {
				accessTokens.add(gaeOAuthAccessToken.getToken());
			} catch (JDOException e) {
				this.accessTokens.deleteByTokenId(gaeOAuthAccessToken.getTokenId());
			}
		}

		accessTokens = removeNulls(accessTokens);

		return accessTokens;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();

		List<GaeOAuthAccessToken> gaeOAuthAccessTokens = this.accessTokens.findByUsernameAndClientId(userName, clientId);

		if (gaeOAuthAccessTokens.isEmpty()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find access token for userName " + userName);
			}
		}

		for (GaeOAuthAccessToken gaeOAuthAccessToken : gaeOAuthAccessTokens) {
			try {
				accessTokens.add(gaeOAuthAccessToken.getToken());
			} catch (JDOException e) {
				this.accessTokens.deleteByTokenId(gaeOAuthAccessToken.getTokenId());
			}
		}
		accessTokens = removeNulls(accessTokens);

		return accessTokens;
	}

	private List<OAuth2AccessToken> removeNulls(List<OAuth2AccessToken> accessTokens) {
		List<OAuth2AccessToken> tokens = new ArrayList<OAuth2AccessToken>();
		for (OAuth2AccessToken token : accessTokens) {
			if (token != null) {
				tokens.add(token);
			}
		}
		return tokens;
	}

	protected String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}
}
