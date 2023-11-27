package com.wealth.fly.core.model;

import lombok.Data;

/**
 * @author : lisong
 * @date : 2023/5/12
 */
@Data
public class Account {

    private String id;
    private String type;
    private String secretKey;
    private String accessKey;
    private String passphrase;

}