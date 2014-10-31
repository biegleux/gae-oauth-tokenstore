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
 */

package com.github.biegleux.gae.oauth.tokenstore.persistence.model;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * This class represents the persistent entity for storing {@link OAuth2AccessToken}.
 */
@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class GaeOAuthAccessToken extends GaeOAuthToken<OAuth2AccessToken> {

	@Persistent
	private String authenticationId;

	@Persistent
	private String username;

	@Persistent
	private String clientId;

	@Persistent
	private String refreshToken;

	public GaeOAuthAccessToken() {
	}

	public void setAuthenticationId(String authenticationId) {
		this.authenticationId = authenticationId;
	}

	public String getAuthenticationId() {
		return authenticationId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
}
