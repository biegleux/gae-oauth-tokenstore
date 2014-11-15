GaeTokenStore
=============
Implementation of Spring token services that stores OAuth2 tokens in Google App Engine Datastore.

Installation
------------

Add the following dependency to your `pom.xml`:
```
<dependency>
	<groupId>com.github.biegleux</groupId>
	<artifactId>gae-oauth-tokenstore</artifactId>
	<version>0.2</version>
</dependency>
```

*GaeTokenStore* has following dependencies, these should be already installed:

```
<dependency>
	<groupId>com.google.appengine</groupId>
	<artifactId>appengine-api-1.0-sdk</artifactId>
	<version>1.9.14</version>
</dependency>

<dependency>
	<groupId>org.springframework.security.oauth</groupId>
	<artifactId>spring-security-oauth2</artifactId>
	<version>2.0.3.RELEASE</version>
</dependency>

<dependency>
	<groupId>javax.jdo</groupId>
	<artifactId>jdo-api</artifactId>
	<version>3.0.1</version>
</dependency>

<dependency>
	<groupId>org.datanucleus</groupId>
	<artifactId>datanucleus-api-jdo</artifactId>
	<version>3.1.3</version>
</dependency>
```

Usage
-----
Provide *GaeTokenStore* instance to your OAuth2 Authorization Server configuration. Following will replace default *InMemoryTokenStore* with *GaeTokenStore* instance:

```
import com.github.biegleux.gae.oauth.tokenstore.GaeTokenStore;
...

@Configuration
@EnableAuthorizationServer
protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private TokenStore tokenStore;

	...

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore).authenticationManager(authenticationManager);
	}

	@Bean
	public TokenStore tokenStore() {
		return new GaeTokenStore();
	}

	...
```
