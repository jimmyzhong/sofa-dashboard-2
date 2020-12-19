package xyz.lot.dashboard.manage.security.service;

import lombok.extern.slf4j.Slf4j;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.common.constants.Global;
import xyz.lot.dashboard.common.constants.ShiroConstants;
import xyz.lot.dashboard.common.constants.SystemConstants;
import xyz.lot.dashboard.common.constants.UserConstants;
import xyz.lot.dashboard.manage.entity.SysUser;
import xyz.lot.dashboard.common.expection.*;
import xyz.lot.dashboard.common.expection.user.UserBlockedException;
import xyz.lot.dashboard.common.expection.user.UserDeleteException;
import xyz.lot.dashboard.common.expection.user.UserNotFoundException;
import xyz.lot.dashboard.common.expection.user.UserPasswordNotMatchException;
import xyz.lot.dashboard.manage.service.SysUserService;
import xyz.lot.dashboard.manage.factory.AsyncManager;
import xyz.lot.dashboard.manage.factory.AsyncFactory;
import xyz.lot.dashboard.common.util.MessageUtil;
import xyz.lot.dashboard.common.util.ServletUtil;
import xyz.lot.dashboard.manage.security.UserInfoContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.lot.dashboard.common.expection.CaptchaException;

/**
 * 登录校验方法
 */
@Component
@Slf4j
public class LoginService {
    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     */
    public SysUser login(String username, String password) {

        if(Global.isDebugMode()) {
            String debugUserName = Global.getDebugLoginName();
            String debugPassword = Global.getDebugPassword();
            if(!org.apache.commons.lang3.StringUtils.equals(debugUserName,username)) {
                throw BusinessException.build("维护中，"+username+"不能登录");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(debugPassword,password)) {
                throw BusinessException.build("维护中，"+username+"密码不正确，不能登录");
            }
            SysUser user = sysUserService.findUserByLoginName(username);
            if (user == null) {
                throw BusinessException.build("虚拟" + username + "不存在");
            }
            log.info("DebugMode用户{}登录成功",username);
            return user;
        }
        // 验证码校验
        if (!StringUtils.isEmpty(ServletUtil.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA))) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("not.null")));
            throw new UserNotFoundException(username);
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("用户名长度不正确，长度应该在"+ UserConstants.USERNAME_MIN_LENGTH +"和" + UserConstants.USERNAME_MAX_LENGTH + "之间",username);
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("密码长度不正确，长度应该在"+ UserConstants.PASSWORD_MIN_LENGTH +"和" + UserConstants.PASSWORD_MAX_LENGTH + "之间",username);
        }
        // 查询用户信息
        SysUser user = sysUserService.findUserByLoginName(username);

        if (user == null && maybeMobilePhoneNumber(username)) {
            user = sysUserService.findUserByPhoneNumber(username);
        }

        if (user == null && maybeEmail(username)) {
            user = sysUserService.findUserByEmail(username);
        }

        if (user == null) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.not.exists")));
            throw new UserNotFoundException(username);
        }

        if (Boolean.TRUE.equals(user.getIsDelete())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserConstants.USER_DELETED.equals(user.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", user.getRemark())));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        user = sysUserService.recordLoginIp(user.getUserId(), UserInfoContextHelper.getIp());
        return user;
    }

    private boolean maybeEmail(String username) {
        if (!username.matches(UserConstants.EMAIL_PATTERN)) {
            return false;
        }
        return true;
    }

    private boolean maybeMobilePhoneNumber(String username) {
        if (!username.matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN)) {
            return false;
        }
        return true;
    }

}
