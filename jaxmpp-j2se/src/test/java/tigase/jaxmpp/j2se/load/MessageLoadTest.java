package tigase.jaxmpp.j2se.load;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.SessionEstablishmentModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.WatchedXmpp;

public class MessageLoadTest {
	/**
	 * if("sankuai.info".equals(hostname)){
			return java.util.Arrays.asList(new Entry("192.168.2.160", 5222));
		}
	 */
	public static String[] getAccounts(int offset,int limit) throws ClassNotFoundException, SQLException{
		List<String> re=new ArrayList<String>();
		
		DriverManager.class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://cosdb:3306/meituanxm?user=q3boy&password=q3girl&useUnicode=true&characterEncoding=UTF-8&autoCreateUser=true");
		ResultSet rs=con.createStatement().executeQuery("select substring(user_id,1,locate('@',user_id)-1) from tig_users where user_pw='1' and user_id like '%@sankuai.info' and user_id not in('zhangdongxiao@sankuai.info','liule@sankuai.info','jianjingbao@sankuai.info','luodandan@sankuai.info','lishuai02@sankuai.info','') limit "+offset+","+limit);
		
		while(rs.next()){
			re.add(rs.getString(1));
		}
		
		rs.close();
		con.close();
		return re.toArray(new String[re.size()]);
	}
	private static final String AUTO_PREFIX="Auto Reply To:";
	private static final AtomicInteger sended=new AtomicInteger();
	private static final AtomicInteger active=new AtomicInteger();
	private static final AtomicInteger received=new AtomicInteger();
	public static void main(String[] args) throws ClassNotFoundException, SQLException, JaxmppException{
		//1s随机发送消息
		final String[] accounts=getAccounts(0,200);
		WatchedXmpp[] xms=new WatchedXmpp[accounts.length];
		
		for(int i=0;i<accounts.length;i++){
			String account=accounts[i];
			
			final WatchedXmpp xm=xms[i]=new WatchedXmpp();
//			xm.getSessionObject().setUserProperty(SessionObject.DOMAIN_NAME, "sankuai.info");
//			xm.getSessionObject().setUserProperty(SessionObject.SERVER_NAME, "t7");
	        xm.getSessionObject().setUserProperty(SessionObject.USER_BARE_JID,
	                BareJID.bareJIDInstance(account, "sankuai.info"));
	        xm.getSessionObject().setUserProperty(SessionObject.PASSWORD, "1");
	        xm.getSessionObject().setUserProperty(SessionObject.RESOURCE, "xm");
	        
	        
	        
	        //vCard完善。
	        SessionEstablishmentModule session=xm.getModule(SessionEstablishmentModule.class);
	        session.addListener(SessionEstablishmentModule.SessionEstablishmentSuccess,new Listener<SessionEstablishmentModule.SessionEstablishmentEvent>() {
	            @Override
	            public void handleEvent(SessionEstablishmentModule.SessionEstablishmentEvent me) throws JaxmppException {
	            	System.out.println("SessionEstablishmentSuccess.........");
	            	MessageModule mm=xm.getModule(MessageModule.class);
	    	        mm.addListener(MessageModule.MessageReceived,new Listener<MessageEvent>(){

	    				@Override
	    				public void handleEvent(MessageEvent be) throws JaxmppException {
	    					Message rm=be.getMessage();
	    					if(!rm.getBody().startsWith(AUTO_PREFIX)){
	    						Message msg = Message.create();
	    	                    msg.setBody("Auto Reply To:"+rm.getBody());
	    	                    msg.setFrom(xm.getSessionObject().getBindedJid());
	    	                    msg.setTo(rm.getFrom());
	    	                    msg.setId(UIDGenerator.next());
	    	                    msg.setType(StanzaType.chat);
//	    	                    Element ele=new DefaultElement("req",null,"jabber:client");
//			                    ele.setAttribute("id", msg.getId());
//			                    msg.addChild(ele);
	    	                    xm.send(msg);
	    	                    sended.incrementAndGet();
	    					}
	    					List<Element> res=rm.getChildren("req");
	    					if(res!=null&&res.size()==1){
	    						Element ack=new DefaultElement("ack",null,"jabber:client");
	    						ack.setAttribute("id", res.get(0).getAttribute("id"));
	    						//xm.getConnector().send(ack);
	    					}
	    					received.incrementAndGet();
	    				}
	    	        });
	            	while(true){
	            		if(xm.isConnected()){
	            			int index=(int)(Math.random()*accounts.length);
			            	String to=accounts[index]+"@sankuai.info/xm";
			            	Message msg = Message.create();
		                    msg.setBody("Hello:"+to);
		                    msg.setFrom(xm.getSessionObject().getBindedJid());
		                    msg.setTo(JID.jidInstance(to));
		                    msg.setId(UIDGenerator.next());
		                    msg.setType(StanzaType.chat);
//		                    Element ele=new DefaultElement("req",null,"jabber:client");
//		                    ele.setAttribute("id", msg.getId());
//		                    msg.addChild(ele);
		                    
		                    xm.send(msg);
		                    sended.incrementAndGet();
		            		try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	            		}else{
		            		try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	            		}
	            	}
	            }
	        });
	        xm.addListener(Jaxmpp.Connected,new Listener<JaxmppEvent>() {

				@Override
				public void handleEvent(JaxmppEvent be) throws JaxmppException {
					System.out.println("Connected.........");
					active.incrementAndGet();
				}
	        	
	        });
	        xm.addListener(Jaxmpp.Disconnected,new Listener<JaxmppEvent>() {

				@Override
				public void handleEvent(JaxmppEvent be) throws JaxmppException {
					System.out.println("Disconnected.........");
					active.decrementAndGet();
				}
	        	
	        });
	        		
	        
	        xm.login();
		}

        
        new Thread(){
        	public void run(){
        		int lastSended=sended.intValue();
        		int lastReceived=received.intValue();
        		while(true){
        			try {
						Thread.sleep(10000);
						System.out.println(".........................................");
						System.out.println("Active:"+active.intValue()+"/"+accounts.length);

		        		int newSended=sended.intValue();
		        		int newReceived=received.intValue();
						System.out.println("Sended:"+newSended+",tps="+(newSended-lastSended)/10);
						System.out.println("Received:"+newReceived+",tps="+(newReceived-lastReceived)/10);
						lastSended=newSended;
						lastReceived=newReceived;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        	}
        }.start();
		
	}
}
//不是说不需要优化，或者更不是说大家搞不定，控制步伐。
//组织结构怎么处理
//http://wiki.sankuai.com/pages/viewpage.action?pageId=65242985