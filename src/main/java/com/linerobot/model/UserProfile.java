package com.linerobot.model;

import lombok.Data;

@Data
public class UserProfile {

    private String displayName;

    private String userId;

    private String pictureUrl;

    private String language;
}
