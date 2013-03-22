package tigase.jaxmpp.j2se.load;

import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.SessionEstablishmentModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.WatchedXmpp;

public class MessageLoadTest {
	private static final int MIN_MSG_PERIOD=1;
	private static final int MAX_MSG_PERIOD=1;
	private static final int MSG_LENGTH=50;
	private static final String DOMAIN="sankuai.net";
	private static final String IGNORES="zhangdongxiao,liule,jianjingbao,lishuai02,wanglujing,luodandan,panmingwei,wangkang";
	private static final int BATCH_SEQ=0;
	private static final int BATCH_SIZE=200;
	public static String[] getAccounts(int offset,int limit) throws ClassNotFoundException, SQLException{
		List<String> re=new ArrayList<String>();
		String[] ignores=IGNORES.split("[,，\\s]+");
		
		String query="select user_id from tig_users where user_pw='1' and user_id like '%@"+DOMAIN+"' and user_id not in (";
		for(String ignore:ignores){
			query+="'"+ignore+"@"+DOMAIN+"',";
		}
		query+="'') limit "+offset+","+limit;
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://cosdb:3306/meituanxm?user=q3boy&password=q3girl&useUnicode=true&characterEncoding=UTF-8&autoCreateUser=true");
		ResultSet rs=con.createStatement().executeQuery(query);
		
		while(rs.next()){
			re.add(rs.getString(1));
		}
		
		rs.close();
		con.close();
		return re.toArray(new String[re.size()]);
	}
		
	private static final String AUTO_PREFIX="Auto Reply To:";
	private static final AtomicInteger sended=new AtomicInteger();
	private static final AtomicInteger received=new AtomicInteger();
	private static final AtomicInteger acted=new AtomicInteger();
	public static void main(String[] args) throws ClassNotFoundException, SQLException, JaxmppException{
		//1s随机发送消息
		final String[] accounts=getAccounts(BATCH_SIZE*BATCH_SEQ,BATCH_SIZE);
		final WatchedXmpp[] xms=new WatchedXmpp[accounts.length];
	    final Criteria CRIT = ElementCriteria.name("ack");
		for(int i=0;i<accounts.length;i++){
			String account=accounts[i];
			
			final WatchedXmpp xm=xms[i]=new WatchedXmpp();
			xm.getModulesManager().register(new XmppModule(){

				@Override
				public Criteria getCriteria() {
					return CRIT;
				}

				@Override
				public String[] getFeatures() {
					return null;
				}

				@Override
				public void process(Element element) throws XMPPException,
						XMLException, JaxmppException {
					//Just Ignore
					acted.incrementAndGet();
				}
				
			});
	        xm.getSessionObject().setUserProperty(SessionObject.USER_BARE_JID,BareJID.bareJIDInstance(account));
	        xm.getSessionObject().setUserProperty(SessionObject.PASSWORD, "1");
	        xm.getSessionObject().setUserProperty(SessionObject.RESOURCE, "xm");
	        
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
	                    
	                    Element ele=new DefaultElement("req",null,"jabber:client");
	                    ele.setAttribute("id", msg.getId());
	                    msg.addChild(ele);
	                    
	                    xm.send(msg);
	                    sended.incrementAndGet();
					}
					if(rm.getChildren("req").size()>0){
	                    Element req=rm.getChildren("req").get(0);
	                    Element ack=new DefaultElement("ack");
	                    ack.setAttribute("id", req.getAttribute("id"));
	                    
	                    xm.getConnector().send(ack);
	                }
					received.incrementAndGet();
				}
	        });
	        
	        //vCard完善。
	        SessionEstablishmentModule session=xm.getModule(SessionEstablishmentModule.class);
	        session.addListener(SessionEstablishmentModule.SessionEstablishmentSuccess,new Listener<SessionEstablishmentModule.SessionEstablishmentEvent>() {
	        	private Thread sender=new Thread(){
    	        	public void run(){
    	        		while(true){
    	            		if(xm.isConnected()){
    		            		try {
    		            			int index=(int)(Math.random()*accounts.length);
	    			            	String to=accounts[index]+"/xm";
	    			            	Message msg = Message.create();
	    			            	String body=Base64.encode(new byte[MSG_LENGTH]);
	    		                    msg.setBody("Hello:"+body);
	    		                    msg.setFrom(xm.getSessionObject().getBindedJid());
	    		                    msg.setTo(JID.jidInstance(to));
	    		                    msg.setId(UIDGenerator.next());
	    		                    msg.setType(StanzaType.chat);
	    		                    
	    		                    Element ele=new DefaultElement("req",null,"jabber:client");
	    		                    ele.setAttribute("id", msg.getId());
	    		                    msg.addChild(ele);
	    		                    
	    		                    xm.send(msg);
	    		                    sended.incrementAndGet();
	    		                    
    		            			int sleep=(int)(MIN_MSG_PERIOD+(MAX_MSG_PERIOD-MIN_MSG_PERIOD)*Math.random());
    								Thread.sleep(sleep*1000);
    							} catch (Throwable th) {
    								th.printStackTrace();
    							}
    	            		}else{
    		            		try {
    								Thread.sleep(10000);
    		            		} catch (Throwable th) {
    								th.printStackTrace();
    							}
    	            		}
    	            	}
    	        	}
    	        };
	            @Override
	            public void handleEvent(SessionEstablishmentModule.SessionEstablishmentEvent me) throws JaxmppException {
	            	System.out.println("SessionEstablishmentSuccess.........");
	            	synchronized(sender){
		    	        if(sender.getState()==State.NEW){
		    	        	sender.start();
		    	        }
	            	}
	            }
	        });
	        		
	        
	        xm.login();
		}

        
        new Thread(){
        	public void run(){
        		int lastSended=sended.intValue();
        		int lastActed=acted.intValue();
        		int lastReceived=received.intValue();
        		while(true){
        			try {
						Thread.sleep(10000);
						System.out.println(".........................................");
						int active=0;
						for(Jaxmpp xm:xms){
							if(xm.isConnected()){
								active++;
							}
						}
		        		int newSended=sended.intValue();
		        		int newReceived=received.intValue();
		        		int newActed=acted.intValue();
						System.out.println("Active:"+active+"/"+accounts.length);
						System.out.println("Sended:"+newSended+",tps="+(newSended-lastSended)/10);
						System.out.println("Acted:"+newActed+",tps="+(newActed-lastActed)/10);
						System.out.println("Received:"+newReceived+",tps="+(newReceived-lastReceived)/10);
						lastSended=newSended;
						lastActed=newActed;
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