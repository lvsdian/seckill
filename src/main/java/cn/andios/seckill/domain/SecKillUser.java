package cn.andios.seckill.domain;

import lombok.*;

import java.util.Date;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/16:15
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SecKillUser {
    private Long id;
    private String nickname;
    private String password;
    private String salt;
    private String head;
    private Date registerDate;
    private Date lastLoginDate;
    private Integer loginCount;
}
