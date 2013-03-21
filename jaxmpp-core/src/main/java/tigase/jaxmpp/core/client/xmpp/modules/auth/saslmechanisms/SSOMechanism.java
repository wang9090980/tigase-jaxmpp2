package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

/**
 * Created with IntelliJ IDEA.
 * User: zhangdongxiao
 * Date: 13-3-18
 * Time: PM4:27
 * To change this template use File | Settings | File Templates.
 */
public class SSOMechanism implements SaslMechanism {
    private static final String NULL = String.valueOf((char) 0);
    public static final String XSID="xsid";
    //xsid'0':url
    @Override
    public String evaluateChallenge(String input, SessionObject sessionObject) {
        if(input==null){
            String xsid=sessionObject.getProperty(XSID);
            if(xsid!=null){
                return Base64.encode(xsid.getBytes());
            }else{
                return "";
            }
        }else{
            String[] data = new String(Base64.decode(input)).split(NULL);
            if (data.length != 2)
                throw new IllegalArgumentException("Invalid number of message parts");
            String xsid=data[0];
            String ssourl=data[1];
            sessionObject.setProperty(XSID,xsid);
            //持久化存储
            //打开登录页面
            System.out.println(ssourl);
            return null;
        }
    }
    @Override
    public boolean isAllowedToUse(SessionObject sessionObject) {
        return true;
    }
    @Override
    public String name() {
        return "SSO";
    }
}