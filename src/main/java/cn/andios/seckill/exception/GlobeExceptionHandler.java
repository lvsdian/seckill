package cn.andios.seckill.exception;

import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:异常处理
 * @author:LSD
 * @when:2019/10/15/17:10
 */
@ControllerAdvice
@ResponseBody
public class GlobeExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        e.printStackTrace();
        if(e instanceof BindException){
            BindException bindException = (BindException)e;
            List<ObjectError> errors = bindException.getAllErrors();
            ObjectError objectError = errors.get(0);
            String msg = objectError.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else if (e instanceof GlobeException){
            GlobeException globeException = (GlobeException)e;
            return Result.error(globeException.getCodeMsg());
        }else{
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
