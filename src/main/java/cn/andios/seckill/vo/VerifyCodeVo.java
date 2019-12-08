package cn.andios.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.image.BufferedImage;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/23/16:40
 */
@Data
@AllArgsConstructor
public class VerifyCodeVo {
    private Integer result;
    private BufferedImage bufferedImage;

}
