package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthEvent;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 *
 * @author zhangdongxiao
 * @created Jan 14, 2013
 *
 * @version 1.0
 */
public class WatchedJaxmppTest {
    
    public static void main(String[] args) throws JaxmppException, InterruptedException{
        final WatchedXmpp xm=new WatchedXmpp();
//        xm.getSessionObject().setUserProperty(SessionObject.DOMAIN_NAME, "sankuai.info");
        xm.getSessionObject().setUserProperty(SessionObject.RESOURCE, "xm");
        xm.getSessionObject().setUserProperty(SessionObject.PASSWORD, "1");
        xm.getSessionObject().setUserProperty(SessionObject.USER_BARE_JID,
                BareJID.bareJIDInstance("chenliang", "sankuai.info"));

        
        xm.addListener(new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) throws JaxmppException {
                if(be instanceof MessageEvent){
                    Message rm=((MessageEvent)be).getMessage();
                    
                    Message msg = Message.create();
                    msg.setBody("Auto Reply To:"+rm.getBody());
                    msg.setTo(rm.getFrom());
                    msg.setId(UIDGenerator.next());
                    msg.setType(StanzaType.chat);
                    xm.send(msg);
                }
            }
        });

        
        xm.addListener(Jaxmpp.Connected,new Listener<JaxmppEvent>() {

            @Override
            public void handleEvent(JaxmppEvent be) throws JaxmppException {
                DefaultElement ele=new DefaultElement("iq");
                ele.setAttribute("type", "get");
                
                DefaultElement query=new DefaultElement("query");
                query.setXMLNS("jabber:iq:roster");
                
                ele.addChild(query);
                
                System.out.println("RosterResponse="+xm.call(IQ.create(ele)).getAsString());
                
                MucModule muc=xm.getModule(MucModule.class);
                muc.addListener(MucModule.MucMessageReceived,new Listener<MucModule.MucEvent>() {
                    @Override
                    public void handleEvent(MucModule.MucEvent be) throws JaxmppException {
                            Message rm=be.getMessage();
                            if(!"ChenLiang".equals(rm.getFrom().getResource())
                                    &&!rm.getBody().contains("Auto Muc To:")){
                                be.getRoom().sendMessage("Auto Muc To:"+rm.getBody());
                            }
                    }
                });
                
                muc.join("god", "muc.sankuai.info", "ChenLiang");
            }
            
        });
        


        AuthModule auth=xm.getModule(AuthModule.class);
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
        

        
        xm.login();

        new Thread(){
            public void run(){
                while(true){
                    try{
                        Thread.sleep(10000);

                        double splitor=Math.random();
                        if(splitor>0.75){
                            xm.setConnectable(true);
                        }else if(splitor>0.5){
                            xm.setConnectable(false);
                        }else if(splitor>0.25){
                            xm.login();
                        }else{
                            xm.logout();
                        }
                    }catch(Throwable th){

                    }

                }
            }
        }.start();
        
    }
    
}
