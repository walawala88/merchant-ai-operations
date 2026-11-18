package com.merchant.ops.unit;

import com.merchant.ops.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void shouldCreateAndParseSignedToken() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("empId", 7L);
        String token = JwtUtil.createJWT("unit-test-secret", 60_000L, claimsMap);
        Claims claims = JwtUtil.parseJWT("unit-test-secret", token);

        assertThat(claims.get("empId").toString()).isEqualTo("7");
        assertThat(claims.getExpiration()).isNotNull();
    }
}