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

package com.github.biegleux.gae.oauth.tokenstore.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.github.biegleux.gae.oauth.tokenstore.persistence.model.GaeOAuthAccessToken;

/**
 * This class provides operations for managing {@link GaeOAuthTokenRepository} entities.
 */
public class GaeOAuthAccessTokenRepository extends GaeOAuthTokenRepository<GaeOAuthAccessToken> {

	public GaeOAuthAccessTokenRepository() {
		super(GaeOAuthAccessToken.class);
	}

	/**
	 * Looks up the {@link GaeOAuthAccessToken} entities with the given client ID.
	 * @param clientId Client ID.
	 * @return List of the corresponding {@link GaeOAuthAccessToken} entities.
	 */
	@SuppressWarnings("unchecked")
	public List<GaeOAuthAccessToken> findByClientId(String clientId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(GaeOAuthAccessToken.class);
			query.setFilter("clientId == param");
			query.declareParameters("String param");
			return new ArrayList<GaeOAuthAccessToken> ((Collection<GaeOAuthAccessToken>) query.execute(clientId));
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}

	/**
	 * Looks up the {@link GaeOAuthAccessToken} entities with the given user name and client ID.
	 * @param username User name.
	 * @param clientId Client ID.
	 * @return List of the corresponding {@link GaeOAuthAccessToken} entities.
	 */
	@SuppressWarnings("unchecked")
	public List<GaeOAuthAccessToken> findByUsernameAndClientId(String username, String clientId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(GaeOAuthAccessToken.class);
			query.setFilter("username == param1 && clientId == param2");
			query.declareParameters("String param1, String param2");
			return new ArrayList<GaeOAuthAccessToken> ((Collection<GaeOAuthAccessToken>) query.execute(username, clientId));
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}

	/**
	 * Looks up the {@link GaeOAuthAccessToken} entities with the given refresh token.
	 * @param refreshToken Refresh token.
	 * @return List of the corresponding {@link GaeOAuthAccessToken} entities.
	 */
	@SuppressWarnings("unchecked")
	public List<GaeOAuthAccessToken> findByRefreshToken(String refreshToken) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(GaeOAuthAccessToken.class);
			query.setFilter("refreshToken == param");
			query.declareParameters("String param");
			return new ArrayList<GaeOAuthAccessToken> ((Collection<GaeOAuthAccessToken>) query.execute(refreshToken));
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}
}
