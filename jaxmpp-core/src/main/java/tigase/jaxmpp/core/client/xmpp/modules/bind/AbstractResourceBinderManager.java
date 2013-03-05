package tigase.jaxmpp.core.client.xmpp.modules.bind;

/**
 * Created with IntelliJ IDEA.
 * User: wangkang
 * Date: 13-3-5
 * Time: 下午12:51
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResourceBinderManager {

    public abstract String getXsid();

    public abstract void setXsid(String xsid);
}
