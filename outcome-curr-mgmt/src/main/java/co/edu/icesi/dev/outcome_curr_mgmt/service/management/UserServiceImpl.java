package co.edu.icesi.dev.outcome_curr_mgmt.service.management;

import co.edu.icesi.dev.outcome_curr.mgmt.model.stdindto.management.LoginInDTO;
import co.edu.icesi.dev.outcome_curr.mgmt.model.stdoutdto.management.LoginOutDTO;
import co.edu.icesi.dev.outcome_curr_mgmt.exception.OutCurrException;
import co.edu.icesi.dev.outcome_curr_mgmt.exception.OutCurrExceptionType;
import co.edu.icesi.dev.outcome_curr_mgmt.model.entity.management.User;
import co.edu.icesi.dev.outcome_curr_mgmt.persistence.management.UserRepository;
import co.edu.icesi.dev.outcome_curr_mgmt.saamfi.delegate.SaamfiClient;
import co.edu.icesi.dev.outcome_curr_mgmt.saamfi.util.SaamfiJwtTools;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // TODO add test coverage
    private final UserRepository userRepository;
    private final SaamfiClient saamfiClient;
    private final SaamfiJwtTools saamfiJwtTools;

    // Micrometer registry for metrics
    private final MeterRegistry meterRegistry;

    @Transactional
    @Override
    public LoginOutDTO login(LoginInDTO loginInDTO) {
        logger.info("Login attempt started for user: {}", loginInDTO.username());
        // Increment a counter for login attempts
        meterRegistry.counter("user_service.login.attempts").increment();

        try {
            LoginOutDTO loginOutDTO = saamfiClient.getUserLogin(loginInDTO);
            logger.info("Login successful for user ID: {}", loginOutDTO.userId());

            // Validate token permissions
            if (!saamfiJwtTools.tokenHasPermission(loginOutDTO.accessToken(), "ROLE_Access-system")) {
                logger.error("Invalid permissions for user ID: {}", loginOutDTO.userId());
                meterRegistry.counter("user_service.login.invalid_permissions").increment();
                throw new OutCurrException(OutCurrExceptionType.USER_INVALID_PERMISSIONS);
            }

            // Check if user exists in the database
            if (userRepository.findById(loginOutDTO.userId()).isEmpty()) {
                logger.info("User ID {} not found in database, creating a new record.", loginOutDTO.userId());
                User userEntity = User.builder()
                        .usrId(loginOutDTO.userId())
                        .usrName(loginOutDTO.userName() + " " + loginOutDTO.userLastname())
                        .usrEmail(loginOutDTO.userEmail())
                        .usrIsActive('Y')
                        .build();
                userRepository.save(userEntity);
                logger.info("User ID {} saved to database.", loginOutDTO.userId());
            } else {
                logger.info("User ID {} already exists in the database.", loginOutDTO.userId());
            }

            // Increment a counter for successful logins
            meterRegistry.counter("user_service.login.success").increment();

            return loginOutDTO;
        } catch (OutCurrException e) {
            logger.error("Login failed for user: {}. Reason: {}", loginInDTO.username(), e.getMessage());
            meterRegistry.counter("user_service.login.failures").increment();
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {}", loginInDTO.username(), e);
            meterRegistry.counter("user_service.login.errors").increment();
            throw e;
        }
    }

}
