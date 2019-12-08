package cn.andios.seckill.vo;

import cn.andios.seckill.validator.IsMobile;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/15:49
 */
@Data
public class LoginVo {
    /**
     * 这里的@IsMobile是validator包下自定义的注解
     */
    @NotNull
    @IsMobile
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;
}
