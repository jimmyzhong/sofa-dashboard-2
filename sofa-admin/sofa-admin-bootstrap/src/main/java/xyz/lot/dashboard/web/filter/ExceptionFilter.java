package xyz.lot.dashboard.web.filter;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.common.domain.ResponseContainer;
import xyz.lot.dashboard.common.expection.user.UserException;
import xyz.lot.dashboard.common.expection.user.UserHasNotPermissionException;
import xyz.lot.dashboard.common.util.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Component
@Order(0)
public class ExceptionFilter implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

        //httpServletResponse.getWriter().write("wrong" + e.getMessage());
        //httpServletResponse.flushBuffer();

        int code = ResponseContainer.FAIL_CODE;
        String msg = "系统异常";
        if (e instanceof UserHasNotPermissionException) {
            UserHasNotPermissionException ce = (UserHasNotPermissionException) e;
            if (StringUtils.isNotBlank(ce.getPermission())) {
                msg = String.format("缺少权限[%s]", ce.getPermission());
            } else {
                msg = ce.getMessage();
            }
        } else if (e instanceof UnauthenticatedException) {
            UnauthenticatedException ce = (UnauthenticatedException) e;
            log.info("请求UnauthenticatedException异常", e);
            msg = "用户未登录:" + ce.getMessage();
        } else if (e instanceof UnauthorizedException) {
            UnauthorizedException ce = (UnauthorizedException) e;
            log.info("请求UnauthorizedException异常", e);
            msg = "用户缺少权限:" + ce.getMessage();
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            msg = String.format("系统不支持请求[%s]", e.getMessage());
        } else if (e instanceof UserException) {
            UserException uexp = (UserException) e;
            log.error("请求{}异常,用户:{},{}",e.getClass().getSimpleName(),uexp.getLoginName(),uexp.getMessage());
            code = uexp.getCode();
            msg = uexp.getMessage();
        } else if (e instanceof BusinessException) {
            log.error("请求BusinessException异常", e);
            BusinessException bexp = (BusinessException) e;
            code = bexp.getCode();
            msg = bexp.getMessage();
        } else if(e instanceof BindException){
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            log.info("请求转换异常 {}",error.toString());
            msg = error.getDefaultMessage();
        } else if(e instanceof DuplicateKeyException){
            DuplicateKeyException ex = (DuplicateKeyException)e;
            String errDetail = ex.getMessage();
            log.error("主键冲突", e);
            msg = "系统异常，请联系管理员。主键冲突";
        } else {
            log.error("请求异常", e);
            String message = e.getMessage();
            if (StringUtils.isNotBlank(message))
                msg = message;
        }

        if (HttpUtil.isAjaxRequest(httpServletRequest)) {
            ModelAndView v = new ModelAndView(new MappingJackson2JsonView());
            v.addObject("code", code);
            v.addObject("msg", msg);
            return v;
        } else {
            ModelAndView v = new ModelAndView();
            v.setViewName("error/exception");
            v.addObject("message", msg);
            return v;
        }

    }
}
