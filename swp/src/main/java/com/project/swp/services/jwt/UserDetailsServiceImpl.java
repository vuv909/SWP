package com.project.swp.services.jwt;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.swp.entities.User;
import com.project.swp.repository.UserRepo;



@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepo userRepo;

	public UserDetailsServiceImpl(UserRepo userRepo) {
		super();
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// WRITE LOGIC TO GET USER FROM DB

		Optional<User> optionalUser = userRepo.findFirstByEmail(email);

		if (optionalUser.isEmpty())
			throw new UsernameNotFoundException("User not found ", null);

		return new org.springframework.security.core.userdetails.User(optionalUser.get().getEmail(),
				optionalUser.get().getPassword(), new ArrayList<>());
	}

}
