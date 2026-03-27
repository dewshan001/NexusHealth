package com.nexushelth.services;

import com.nexushelth.dto.LoginRequest;
import com.nexushelth.dto.LoginResponse;
import com.nexushelth.dto.SignupRequest;
import com.nexushelth.dto.UserDTO;
import com.nexushelth.entities.User;
import com.nexushelth.repositories.UserRepository;
import com.nexushelth.utils.JwtTokenProvider;
import com.nexushelth.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
        
        if (user.isEmpty()) {
            return new LoginResponse(null, null, "User not found");
        }
        
        User foundUser = user.get();
        if (!passwordEncoder.matchPassword(loginRequest.getPassword(), foundUser.getPassword())) {
            return new LoginResponse(null, null, "Invalid password");
        }
        
        String token = jwtTokenProvider.generateToken(
                foundUser.getEmail(), 
                foundUser.getId(), 
                foundUser.getRole().toString()
        );
        
        UserDTO userDTO = convertToDTO(foundUser);
        return new LoginResponse(token, userDTO, "Login successful");
    }
    
    public LoginResponse signup(SignupRequest signupRequest) {
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return new LoginResponse(null, null, "Email already exists");
        }
        
        User newUser = new User();
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(passwordEncoder.encodePassword(signupRequest.getPassword()));
        newUser.setFirstName(signupRequest.getFirstName());
        newUser.setLastName(signupRequest.getLastName());
        newUser.setPhone(signupRequest.getPhone());
        newUser.setRole(signupRequest.getRole());
        newUser.setIsActive(true);
        
        User savedUser = userRepository.save(newUser);
        
        String token = jwtTokenProvider.generateToken(
                savedUser.getEmail(), 
                savedUser.getId(), 
                savedUser.getRole().toString()
        );
        
        UserDTO userDTO = convertToDTO(savedUser);
        return new LoginResponse(token, userDTO, "Signup successful");
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        return dto;
    }
}

