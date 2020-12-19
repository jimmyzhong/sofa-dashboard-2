package xyz.lot.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.common.expection.user.UserException;
import xyz.lot.dashboard.common.util.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录验证
 */
@Controller
@Slf4j
public class LoginAdminController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtil.isAjaxRequest(request)) {
            throw BusinessException.build("未登录或登录超时。请重新登录");
        }
        return "login";
    }

    @PostMapping("/login")
    @AjaxWrapper
    public String ajaxLogin(String username, String password, Boolean rememberMe) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return "登录成功";
        } catch (AuthenticationException e) {
            log.error("用户登录异常",e);
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage())) {
                msg = e.getMessage();
            }
            if(e.getCause() instanceof UserException) {
                throw (UserException)e.getCause();
            } else {
                throw BusinessException.build(msg);
            }
            //return msg;
        }
    }

    @GetMapping("/unauth")
    public String unauth() {
        return "error/unauth";
    }
}
