package com.dongjji.como.user.auth;

import com.dongjji.como.common.mail.GoogleMailSender;
import com.dongjji.como.user.auth.provider.FacebookUserInfo;
import com.dongjji.como.user.auth.provider.GoogleUserInfo;
import com.dongjji.como.user.auth.provider.NaverUserInfo;
import com.dongjji.como.user.auth.provider.OAuth2UserInfo;
import com.dongjji.como.user.entity.User;
import com.dongjji.como.user.repository.UserRepository;
import com.dongjji.como.user.type.Gender;
import com.dongjji.como.user.type.UserRole;
import com.dongjji.como.user.type.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final GoogleMailSender googleMailSender;

    @Autowired
    public PrincipalOauth2UserService(UserRepository userRepository, GoogleMailSender googleMailSender) {
        super();
        this.userRepository = userRepository;
        this.googleMailSender = googleMailSender;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        }


        if (oAuth2UserInfo != null) {
            String email = oAuth2UserInfo.getEmail();
            Optional<User> findUser = userRepository.findByEmail(email);

            if (!findUser.isPresent()) {
                String uuid = UUID.randomUUID().toString();

                googleMailSender.sendEmailAuthMail(email, uuid);
                userRepository.save(User.builder()
                        .email(email)
                        .password(BCrypt.hashpw("{OAuthLoginUser}", BCrypt.gensalt()))
                        .birth(LocalDate.now())
                        .status(UserStatus.AVAILABLE)
                        .role(UserRole.USER)
                        .gender(Gender.MEN)
                        .provider(oAuth2UserInfo.getProvider())
                        .providerId(oAuth2UserInfo.getProviderId())
                        .emailAuth(false)
                        .emailAuthKey(uuid)
                        .emailAuthValidationDt(LocalDateTime.now().plusDays(1))
                        .build());
            }
        }

        return super.loadUser(userRequest);
    }
}
