package cn.andios.seckill.exception;

import cn.andios.seckill.result.CodeMsg;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/17:22
 */
public class GlobeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private CodeMsg codeMsg;

    public GlobeException(CodeMsg codeMsg){
        super(codeMsg.toString());
        this.codeMsg = codeMsg;
    }
    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
