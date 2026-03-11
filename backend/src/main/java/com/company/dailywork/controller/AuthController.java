package com.company.dailywork.controller;

import com.company.dailywork.common.model.ApiResponse;
import com.company.dailywork.security.JwtService;
import com.company.dailywork.security.SecurityUser;
import com.company.dailywork.service.UserService;
import com.company.dailywork.web.dto.AuthTokenResponse;
import com.company.dailywork.web.dto.LoginRequest;
import com.company.dailywork.web.dto.ProfileResponse;
import com.company.dailywork.web.dto.RegisterRequest;
import com.company.dailywork.web.dto.RefreshRequest;
import com.company.dailywork.web.dto.ChangeMyPasswordRequest;
import com.company.dailywork.web.dto.UpdateProfileEmploymentRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<ProfileResponse> register(@RequestBody @Valid RegisterRequest request) {
        com.company.dailywork.entity.User user = userService.register(request);
        return ApiResponse.success("Registered successfully", new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
            user.getRole(),
            user.getEmployedCompany()
        ));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(securityUser);
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        return ApiResponse.success(new AuthTokenResponse(accessToken, refreshToken, securityUser.getUser().getRole()));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType) || jwtService.isTokenExpired(refreshToken)) {
            return ApiResponse.fail(401, "Refresh token is invalid or expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            return ApiResponse.fail(401, "Refresh token is invalid");
        }

        SecurityUser securityUser = (SecurityUser) userDetails;
        String newAccessToken = jwtService.generateAccessToken(securityUser);
        String newRefreshToken = jwtService.generateRefreshToken(securityUser);
        return ApiResponse.success(new AuthTokenResponse(newAccessToken, newRefreshToken, securityUser.getUser().getRole()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ApiResponse.success("Logged out", null);
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return ApiResponse.success(new ProfileResponse(
                securityUser.getUser().getId(),
                securityUser.getUser().getUsername(),
                securityUser.getUser().getNickname(),
            securityUser.getUser().getRole(),
            securityUser.getUser().getEmployedCompany()
        ));
    }

    @PutMapping("/profile/employment")
    public ApiResponse<ProfileResponse> updateMyEmployment(@RequestBody @Valid UpdateProfileEmploymentRequest request,
                                                           @AuthenticationPrincipal SecurityUser securityUser) {
        com.company.dailywork.entity.User user = userService.updateMyEmployedCompany(securityUser, request.getEmployedCompany());
        return ApiResponse.success("Employment updated", new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getEmployedCompany()
        ));
    }

    @PutMapping("/profile/password")
    public ApiResponse<Void> changeMyPassword(@RequestBody @Valid ChangeMyPasswordRequest request,
                                              @AuthenticationPrincipal SecurityUser securityUser) {
        userService.changeMyPassword(securityUser, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("Password updated", null);
    }

    @GetMapping("/admin-only")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<String> adminOnly() {
        return ApiResponse.success("Admin resource", "authorized");
    }
}
