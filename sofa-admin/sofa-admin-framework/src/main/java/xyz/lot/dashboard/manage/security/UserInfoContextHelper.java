package xyz.lot.dashboard.manage.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import xyz.lot.common.model.UserInfo;
import xyz.lot.dashboard.common.util.SpringUtil;
import xyz.lot.dashboard.manage.security.service.SysShiroService;
import xyz.lot.dashboard.manage.security.session.OnlineSession;

public class UserInfoContextHelper {

    public static boolean isLogin() {
        return getLoginUser() != null;
    }

    public static String getCurrentLoginName() {
        return getLoginUser() == null ? null : getLoginUser().getLoginName();
    }

    public static Long getCurrentUserId() {
        return getLoginUser() == null ? null : getLoginUser().getUserId();
    }

    public static String getSessionId() {
        return SecurityUtils.getSubject().getSession() == null ? null : String.valueOf(SecurityUtils.getSubject().getSession().getId());
    }

    public static OnlineSession getSession() {
        Subject subject = SecurityUtils.getSubject();
        Session session = SpringUtil.getBean(SysShiroService.class).getSession(subject.getSession().getId());
        OnlineSession onlineSession = (OnlineSession)session;
        return onlineSession;
    }

    public static String getIp() {
        return getSubject().getSession().getHost();
    }


    public static String getCurrentPhoneNumber() {
        return getLoginUser() == null ? null : getLoginUser().getPhoneNumber();
    }

//    public static SysUser getUser() {
//        SysUser user = null;
//        Object obj = getSubject().getPrincipal();
//        if (obj != null) {
//            user = new SysUser();
//            BeanUtils.copyProperties(obj, user);
//        }
//        return user;
//    }

    public static UserInfo getLoginUser() {
        UserInfo user = null;
        Object obj = getSubject().getPrincipal();
        if (obj != null) {
            if(obj instanceof UserInfo) {
                return (UserInfo)obj;
            } else {
                user = new UserInfo();
                BeanUtils.copyProperties(obj, user);
            }
        }
        return user;
    }

    public static void checkScopePermission(String perm, Long deptId){
        getLoginUser().checkScopePermission(perm,deptId);
    }

    public static void checkPermission(String perm){
        if(!getLoginUser().hashPermission(perm)) {
            throw new RuntimeException("缺少数据权限:[" + perm + "]");
        }
    }


    public static void setUser(UserInfo user) {
        Subject subject = getSubject();
        PrincipalCollection principalCollection = subject.getPrincipals();
        String realmName = principalCollection.getRealmNames().iterator().next();
        PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(user, realmName);
        // 重新加载Principal
        subject.runAs(newPrincipalCollection);
    }

    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }
}
