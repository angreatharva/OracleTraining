package com.example.tradingmicroservice.config;

import com.example.commonsecurity.JwtClaims;
import com.example.tradingmicroservice.security.ServiceTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Attaches a SERVICE token to every outbound Feign call Trading makes.
 *
 * <p>Trading deliberately does <em>not</em> forward the end user's token. Its downstream
 * calls include Bank's debit/credit and Portfolio's {@code /internal/trades*}, all of which
 * are closed to end-user tokens precisely so that an investor cannot move money directly.
 * Trading is the component trusted to orchestrate that, so it presents its own identity.</p>
 *
 * <p><strong>Consequence:</strong> downstream services apply no per-user check to these
 * calls - a SERVICE token bypasses ownership rules. Authorization for a trade therefore has
 * to happen at Trading's own controller boundary, before the saga starts. See
 * {@code TradeTransactionController}.</p>
 */
@Configuration
public class FeignServiceTokenConfig {

    @Bean
    public RequestInterceptor serviceTokenInterceptor(ServiceTokenProvider serviceTokenProvider) {
        return template -> {
            if (!template.headers().containsKey(JwtClaims.AUTHORIZATION_HEADER)) {
                template.header(JwtClaims.AUTHORIZATION_HEADER,
                        JwtClaims.BEARER_PREFIX + serviceTokenProvider.currentToken());
            }
        };
    }
}
