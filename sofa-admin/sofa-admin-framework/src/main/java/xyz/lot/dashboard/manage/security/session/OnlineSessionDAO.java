package xyz.lot.dashboard.manage.security.session;

import xyz.lot.dashboard.manage.entity.SysUserOnline;
import xyz.lot.dashboard.manage.security.service.SysShiroService;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

/**
 * 针对自定义的ShiroSession的db操作
 */
public class OnlineSessionDAO extends AbstractSessionDAO {

    @Autowired
    private SysShiroService sysShiroService;

    public OnlineSessionDAO() {
        super();
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        return sysShiroService.getSession(sessionId);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return sysShiroService.getActiveSessions();
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        sysShiroService.saveSession(session);
        return sessionId;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        sysShiroService.saveSession(session);
    }

    /**
     * 当会话过期/停止（如用户退出时）属性等会调用
     */
    @Override
    public void delete(Session session) {
        OnlineSession onlineSession = (OnlineSession) session;
        if (null == onlineSession) {
            return;
        }
        onlineSession.setStatus(SysUserOnline.OnlineStatus.off_line);
        sysShiroService.deleteSession(onlineSession);
    }

}
