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
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * This is the base class for representing persistent token entity.
 *
 * @param <T> The type of the token stored in this entity.
 */
@PersistenceCapable(identityType = IdentityType.DATASTORE)
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class GaeOAuthToken<T> {

	@Persistent
	private String tokenId;

	@Persistent(serialized = "true", defaultFetchGroup = "true")
	private T token;

	@Persistent(serialized = "true", defaultFetchGroup = "true")
	private OAuth2Authentication authentication;

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setToken(T token) {
		this.token = token;
	}

	public T getToken() {
		return token;
	}

	public void setAuthentication(OAuth2Authentication authentication) {
		this.authentication = authentication;
	}

	public OAuth2Authentication getAuthentication() {
		return authentication;
	}
}
