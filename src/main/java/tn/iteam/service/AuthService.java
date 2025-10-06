package tn.iteam.service;

import tn.iteam.model.LoginRequest;
import tn.iteam.model.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
