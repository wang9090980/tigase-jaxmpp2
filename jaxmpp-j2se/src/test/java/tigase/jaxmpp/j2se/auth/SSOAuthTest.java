package tigase.jaxmpp.j2se.auth;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.SessionEstablishmentModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthEvent;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.SSOMechanism;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.Jaxmpp;

//import tigase.jaxmpp.j2se.xmpp.modules.auth.saslmechanisms.SSOModule;

public class SSOAuthTest {
    public static void main(String[] args) throws JaxmppException, InterruptedException {
        final Jaxmpp xm = new Jaxmpp() {
            @Override
            protected void modulesInit() {
                super.modulesInit();    //To change body of overridden methods use File | Settings | File Templates.
                SaslModule saslModule = getModule(SaslModule.class);
                saslModule.addMechanism(new SSOMechanism(observable), true);
            }

        };
        xm.getSessionObject().setUserProperty(SessionObject.DOMAIN_NAME, "sankuai.info");
        xm.getSessionObject().setUserProperty(SessionObject.RESOURCE, "xm");

//        xm.getModulesManager().register(new SSOModule(xm.getSessionObject()));


        AuthModule auth = xm.getModule(AuthModule.class);
        auth.addListener(AuthModule.AuthSuccess, new Listener<AuthEvent>() {
            @Override
            public void handleEvent(AuthEvent be) throws JaxmppException {
                System.out.println("AuthSuccess");
            }
        });
        auth.addListener(AuthModule.AuthFailed, new Listener<AuthEvent>() {
            @Override
            public void handleEvent(AuthEvent be) throws JaxmppException {
                System.out.println("AuthFailed");
            }
        });
        auth.addListener(AuthModule.AuthStart, new Listener<AuthEvent>() {
            @Override
            public void handleEvent(AuthEvent be) throws JaxmppException {
                System.out.println("AuthStart");
            }
        });

        SessionEstablishmentModule sess = xm.getModule(SessionEstablishmentModule.class);
        sess.addListener(SessionEstablishmentModule.SessionEstablishmentError, new Listener<SessionEstablishmentModule.SessionEstablishmentEvent>() {
            @Override
            public void handleEvent(SessionEstablishmentModule.SessionEstablishmentEvent be) throws JaxmppException {
                System.out.println("SessionEstablishmentError");
            }
        });
        sess.addListener(SessionEstablishmentModule.SessionEstablishmentSuccess, new Listener<SessionEstablishmentModule.SessionEstablishmentEvent>() {
            @Override
            public void handleEvent(SessionEstablishmentModule.SessionEstablishmentEvent be) throws JaxmppException {
                System.out.println("SessionEstablishmentSuccess");

                Message msg = Message.create();
                msg.setBody("Auto Message To:");
                msg.setTo(JID.jidInstance("wanglujing@sankuai.info/xm"));
                msg.setId(UIDGenerator.next());
                msg.setType(StanzaType.chat);
                xm.send(msg);
            }
        });


        xm.login();
    }
}