package tigase.jaxmpp.j2se;


import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;



/**
 * 
 * @author zhangdongxiao
 * @created 2013-3-8
 * @since 1.0
 *
 */
public class WatchedXmpp extends Jaxmpp{
	private final Logger log= Logger.getLogger(this.getClass().getName());
    /**
     * 如果是notify mode，可以设置很大
     */
    private static final int WATCH_PERIOD = 10;
    private static final int LOGIN_PERIOD = 10;
    private static final int CONNECT_PERIOD = 10;
    private static final int PING_PERIOD = 60;
    

    private volatile boolean connectable=true;
    private volatile boolean loginable;
    
    public WatchedXmpp(){
        super(new J2SESessionObject());
        AuthModule auth=getModule(AuthModule.class);
        auth.addListener(AuthModule.AuthFailed, new Listener<AuthEvent>() {
            @Override
            public void handleEvent(AuthEvent be) throws JaxmppException {
                log.warning("AuthFailed");
                setLonginable(false);
            }
        });

        watchdog.start();

        addListener(Jaxmpp.Disconnected,new Listener<JaxmppEvent>() {
            @Override
            public void handleEvent(JaxmppEvent be) throws JaxmppException {
                watchdog.interrupt();
            }
        });
    }
    public void setConnectable(boolean connectable){
        this.connectable=connectable;
        watchdog.interrupt();
    }
    private void setLonginable(boolean loginable){
        this.loginable=loginable;
        watchdog.interrupt();
    }
    public void resume(){
        watchdog.interrupt();
    }
    @Override
    protected void onStreamError(ConnectorEvent be) throws JaxmppException {
        super.onStreamError(be);
        if(be.getStreamError()==StreamError.conflict){
            log.warning("Conflict");
            setLonginable(false);
        }
    }
    
    @Override
    public void login(){
        setLonginable(true);
    }
    public void logout() throws JaxmppException{
        loginable=false;
        disconnect();
    }
    
    private final Thread watchdog=new Thread(new Runnable(){
        public void run(){
            while(true){
                log.info("WatchDog:Not Loginable...");
                demoLogin();

                sleep(WATCH_PERIOD);
            }
        }
    });
    
    private void demoLogin() {
        while(loginable){
            log.info("WatchDog:Loginable...");
            demoConnect();

            sleep(LOGIN_PERIOD);
        }
    }

    private void demoConnect() {
        while(loginable&&connectable){
            log.info("WatchDog:Loginable&Connectable...");
            try{
                super.login();
                
                ping();
            }catch(Throwable th){
                //th.printStackTrace();
            }

            sleep(CONNECT_PERIOD);
        }
    }

    private void ping() throws XMLException, JaxmppException {
        while(isConnected()){
            log.finest("WatchDog:Ping...");
            IQ iq = IQ.create();
            iq.setType(StanzaType.get);
            iq.setTo(JID.jidInstance(sessionObject.getUserBareJid().getDomain()));
            iq.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

            send(iq,PING_PERIOD*1000L, new AsyncCallback(){
                long t1=System.currentTimeMillis();
                @Override
                public void onError(Stanza responseStanza, ErrorCondition error)
                        throws JaxmppException {
                    log.info("Error..."+(System.currentTimeMillis()-t1)+"ms");
                }

                @Override
                public void onSuccess(Stanza responseStanza)
                        throws JaxmppException {
                    log.finest("Pong..."+(System.currentTimeMillis()-t1)+"ms");
                }

                @Override
                public void onTimeout() throws JaxmppException {
                    log.warning("Timeout..."+(System.currentTimeMillis()-t1)+"ms");
                    disconnect();
                }
            });

            sleep(PING_PERIOD);
        }
    }
    private void sleep(int seconds){
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            log.info("Watchdog:Resumed...");
        }
    }

    
    public Stanza call(Stanza request) throws JaxmppException{
        SyncCaller caller=new SyncCaller();
        send(request, null, caller);
        return call(request,10*1000);
    }
    public Stanza call(Stanza request,long timeout) throws JaxmppException{
        SyncCaller caller=new SyncCaller();
        send(request, null, caller);
        return caller.get(timeout);
    }

    static class SyncCaller implements AsyncCallback{
        public Stanza get(long timeout) throws JaxmppException{
            synchronized(this){
                if(status==0){
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }

        Stanza response;
        private int status;
        @Override
        public void onError(Stanza responseStanza, ErrorCondition error)
                throws JaxmppException {
            synchronized(this){
                status=2;
                this.response=responseStanza;
                this.notify();
            }
        }

        @Override
        public void onSuccess(Stanza responseStanza) throws JaxmppException {
            synchronized(this){
                status=1;
                this.response=responseStanza;
                this.notify();
            }
        }

        @Override
        public void onTimeout() throws JaxmppException {
            
        }
        
    }
}