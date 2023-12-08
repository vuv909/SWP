package com.project.swp.utils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.project.swp.dto.TokenObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final String SIGNING_KEY = "aLcGQFJB8HdvX1Vj7Imy9wP5gN2bW3EYs6qoKDtuZfTh0k4RxSaMzniUOrpCe";

	private Key getSigninKey() {
		byte[] apiKeySecretBytes = Base64.getEncoder().encode(SIGNING_KEY.getBytes());
		return Keys.hmacShaKeyFor(apiKeySecretBytes);
	}

	public String generateToken(TokenObject tokenObject) {
		return generateToken(new HashMap<>(), tokenObject);
	}

	public String generateRefreshToken(TokenObject tokenObject) {
		return generateRefreshToken(new HashMap<>(), tokenObject);
	}

	private String generateToken(Map<String, Object> extraClaims, TokenObject tokenObject) {
		return Jwts.builder().setClaims(extraClaims).setSubject(tokenObject.toString())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 10000))
				.signWith(getSigninKey(), SignatureAlgorithm.HS256).compact();
	}

	public String generateRefreshToken(Map<String, Object> extraClaims, TokenObject tokenObject) {
		return Jwts.builder().setClaims(extraClaims).setSubject(tokenObject.toString())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 10000)) // 7 day
				.signWith(getSigninKey(), SignatureAlgorithm.HS256).compact();
	}

	//
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
		final Claims claims = extractAllClaims(token);
		return claimsResolvers.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token).getBody();
	}

	//
	public String extractUserName(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	//
	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUserName(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(new Date());
	}
}
