package xyz.lot.dashboard.manage.security.session;

import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.common.constants.ShiroConstants;
import xyz.lot.dashboard.manage.entity.SysUserOnline;
import xyz.lot.dashboard.manage.service.SysUserOnlineService;
import xyz.lot.common.util.DateUtil;
import xyz.lot.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
public class OnlineWebSessionManager extends DefaultWebSessionManager {

    @Override
    public void setAttribute(SessionKey sessionKey, Object attributeKey, Object value) throws InvalidSessionException {
        super.setAttribute(sessionKey, attributeKey, value);
        log.info("session {} set {}={}",sessionKey.getSessionId(),attributeKey,value);
        if (value != null && needMarkAttributeChanged(attributeKey)) {
            OnlineSession s = getOnlineSession(sessionKey);
            s.markAttributeChanged();
        }
//        if(DefaultSubjectContext.PRINCIPALS_SESSION_KEY.equals(attributeKey)) {
//            OnlineSession s = getOnlineSession(sessionKey);
//            if(s.getUserId() == null) {
//                UserInfo userInfo = (UserInfo)((SimplePrincipalCollection)value).getPrimaryPrincipal();
//                log.info("设置userInfo {} set {}={}",sessionKey.getSessionId(),attributeKey,value);
//                s.setUserId(userInfo.getUserId());
//                s.setLoginName(userInfo.getLoginName());
//            }
//        }
    }

    private boolean needMarkAttributeChanged(Object attributeKey) {
        if (attributeKey == null) {
            return false;
        }
        String attributeKeyStr = attributeKey.toString();
        // 优化 flash属性没必要持久化
        if (attributeKeyStr.startsWith("org.springframework")) {
            return false;
        }
        if (attributeKeyStr.startsWith("javax.servlet")) {
            return false;
        }
        if (attributeKeyStr.equals(ShiroConstants.CURRENT_USERNAME)) {
            return false;
        }
        return true;
    }

    @Override
    public Object removeAttribute(SessionKey sessionKey, Object attributeKey) throws InvalidSessionException {
        Object removed = super.removeAttribute(sessionKey, attributeKey);
        if (removed != null) {
            OnlineSession s = getOnlineSession(sessionKey);
            s.markAttributeChanged();
        }

        return removed;
    }

    public OnlineSession getOnlineSession(SessionKey sessionKey) {
        Session obj = doGetSession(sessionKey);
        return (OnlineSession)obj;
    }

    /**
     * 验证session是否有效 用于删除过期session
     */
    @Override
    public void validateSessions() {
        //log.info("检测sessions是否有效...");

        int invalidCount = 0;
        int timeout = (int) this.getGlobalSessionTimeout();
        Date expiredDate = DateUtils.addMilliseconds(new Date(), -timeout);
        //log.info("检测sessions是否有效，过期时间:{}",DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS,expiredDate));

        SysUserOnlineService sysUserOnlineService = SpringUtil.getBean(SysUserOnlineService.class);
        List<SysUserOnline> sysUserOnlineList = sysUserOnlineService.selectOnlineByLastAccessTime(expiredDate);
        if(sysUserOnlineList !=null && sysUserOnlineList.size() >0) {
            //log.info("检测到过期的在线用户数量:{}", sysUserOnlineList.size());
        }
        // 批量过期删除
        List<String> needOfflineIdList = new ArrayList<>();
        List<String> offLineLoginName = new ArrayList<>();
        for (SysUserOnline sysUserOnline : sysUserOnlineList) {
            try {
                SessionKey key = new DefaultSessionKey(sysUserOnline.getSessionId());
                Session session = retrieveSession(key);
                if (session != null) {
                    throw new InvalidSessionException();
                }
            } catch (InvalidSessionException e) {
                //if (log.isDebugEnabled()) {
                    boolean expired = (e instanceof ExpiredSessionException);
                    String msg = "删除 session id=[" + sysUserOnline.getSessionId() + "]"
                            + (expired ? " (expired)" : " (stopped)");
                    log.info(msg);
                //}
                invalidCount++;
                needOfflineIdList.add(sysUserOnline.getSessionId());
                if(StringUtils.isNotBlank(sysUserOnline.getLoginName())) {
                    offLineLoginName.add(sysUserOnline.getLoginName());
                }
            }

        }
        if (needOfflineIdList.size() > 0) {
            try {
                sysUserOnlineService.batchDeleteOnline(needOfflineIdList);
            } catch (Exception e) {
                log.error("批量删除db session error.", e);
            }
        }

        String msg = "完成session校验,过期时间:" + DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS,expiredDate) + ",";
        if (invalidCount > 0) {
            msg += " [" + invalidCount + "] 个session被删除,删除用户名称:" + offLineLoginName.toString();
        } else {
            msg += " 无session被删除.";
        }
        log.info(msg);
    }

    @Override
    protected Collection<Session> getActiveSessions() {
        throw new UnsupportedOperationException("getActiveSessions method not supported");
    }
}
