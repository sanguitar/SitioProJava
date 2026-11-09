package com.example.sitiopro.usuario.mobile;

import com.example.sitiopro.shared.api.ApiErrorResponse;
import com.example.sitiopro.usuario.mobile.dto.MobileLoginRequest;
import com.example.sitiopro.usuario.mobile.dto.MobileRefreshRequest;
import com.example.sitiopro.usuario.mobile.dto.MobileTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação mobile", description = "Access token Bearer e refresh token rotativo")
public class MobileAuthController {

    private final MobileAuthService authService;

    public MobileAuthController(MobileAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica usuário do SQL Server e inicia sessão mobile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public MobileTokenResponse login(@Valid @RequestBody MobileLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Rotaciona refresh token e emite novo par de tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public MobileTokenResponse refresh(@Valid @RequestBody MobileRefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Revoga a sessão mobile e seus refresh/access tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão revogada"),
            @ApiResponse(responseCode = "401", description = "Bearer ou refresh token inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody MobileRefreshRequest request,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new MobileAuthException("BEARER_OBRIGATORIO", "Use o access token Bearer da sessão mobile.",
                    HttpStatus.UNAUTHORIZED);
        }
        Long usuarioId = claimLong(jwt, MobileTokenService.CLAIM_USER_ID);
        String sessaoId = jwt.getClaimAsString(MobileTokenService.CLAIM_SESSION_ID);
        if (usuarioId == null || sessaoId == null) {
            throw new MobileAuthException("TOKEN_INVALIDO", "Access token inválido.", HttpStatus.UNAUTHORIZED);
        }
        authService.logout(usuarioId, sessaoId, request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    private Long claimLong(Jwt jwt, String name) {
        Object value = jwt.getClaim(name);
        return value instanceof Number number ? number.longValue() : null;
    }
}
