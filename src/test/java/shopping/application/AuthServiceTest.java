package shopping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import shopping.auth.TokenProvider;
import shopping.domain.User;
import shopping.dto.LoginRequest;
import shopping.dto.LoginResponse;
import shopping.exception.PasswordNotMatchException;
import shopping.exception.UserNotFoundException;
import shopping.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @DisplayName("로그인 성공시 액세스 토큰 반환")
    @Test
    void loginSuccess() {
        // given
        String mail = "test@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(mail, password);
        User user = new User(1L, mail, password);
        String accessToken = "eeeee.wwww.asasas";

        Mockito.when(userRepository.findByEmail(mail))
                .thenReturn(Optional.of(user));
        Mockito.when(tokenProvider.issueToken(user)).thenReturn(accessToken);

        // when
        LoginResponse loginResponse = authService.login(loginRequest);

        // then
        assertThat(loginResponse.getAccessToken()).isEqualTo(accessToken);
    }

    @DisplayName("유저가 데이터베이스에 없을 떄 예외 발생")
    @Test
    void loginFailWhenUserNotInDB() {
        // given
        String mail = "test@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(mail, password);

        Mockito.when(userRepository.findByEmail(mail))
                .thenReturn(Optional.empty());

        // when & then
        assertThatCode(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @DisplayName("비밀번호 틀렸을 시 예외 발생")
    @Test
    void loginFailWhenPasswordMisMatch() {
        // given
        String mail = "test@example.com";
        String password = "1234";
        String wrongPassword = "wrong password";
        LoginRequest loginRequest = new LoginRequest(mail, wrongPassword);
        User user = new User(1L, mail, password);

        Mockito.when(userRepository.findByEmail(mail))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatCode(() -> authService.login(loginRequest))
                .isInstanceOf(PasswordNotMatchException.class);
    }
}
