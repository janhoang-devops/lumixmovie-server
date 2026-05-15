package com.project.lumix.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String userId;
    private String username;
    private Set<String> role;
    private boolean authenticated;

    /** Trạng thái hội viên Premium – được trả về ngay sau khi login */
    private boolean isPremium;

    /** Ngày hết hạn gói Premium */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime premiumExpiredAt;
}
