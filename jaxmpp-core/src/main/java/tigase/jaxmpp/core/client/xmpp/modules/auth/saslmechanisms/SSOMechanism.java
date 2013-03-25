package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

/**
 * Created with IntelliJ IDEA.
 * User: zhangdongxiao
 * Date: 13-3-18
 * Time: PM4:27
 * To change this template use File | Settings | File Templates.
 */
public class SSOMechanism implements SaslMechanism {
    public static final EventType SSOChallenge = new EventType();
    public static final String XSID = "xsid";
    static final String NULL = String.valueOf((char) 0);
    private final Observable observable;

    public SSOMechanism(Observable observable) {
        this.observable = observable;
    }

    //xsid'0':url
    @Override
    public String evaluateChallenge(String input, SessionObject sessionObject) {
        if (input == null) {
            String xsid = sessionObject.getProperty(XSID);
            if (xsid != null) {
                return Base64.encode(xsid.getBytes());
            } else {
                return "";
            }
        } else {
            String[] data = new String(Base64.decode(input)).split(NULL);
            if (data.length != 2)
                throw new IllegalArgumentException("Invalid number of message parts");
            String xsid = data[0];
            String ssourl = data[1];
            sessionObject.setProperty(XSID, xsid);
            SSOChallengeEvent event = new SSOChallengeEvent(SSOChallenge, sessionObject);
            event.setXsid(xsid);
            event.setSsoUrl(ssourl);
            try {
                observable.fireEvent(event);
            } catch (JaxmppException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean isAllowedToUse(SessionObject sessionObject) {
        return true;
    }

    public String name() {
        return "SSO";
    }

    public static class SSOChallengeEvent extends BaseEvent {

        String ssoUrl;
        String xsid;

        public SSOChallengeEvent(EventType type, SessionObject sessionObject) {
            super(type, sessionObject);
        }

        public String getSsoUrl() {
            return ssoUrl;
        }

        public void setSsoUrl(String ssoUrl) {
            this.ssoUrl = ssoUrl;
        }

        public String getXsid() {
            return xsid;
        }

        public void setXsid(String xsid) {
            this.xsid = xsid;
        }
    }
}