package tigase.jaxmpp.core.client.xmpp.modules.bind;

/**
 * Created with IntelliJ IDEA.
 * User: wangkang
 * Date: 13-3-5
 * Time: 下午7:23
 * To change this template use File | Settings | File Templates.
 */
public class DefaultResourceBinderManager extends AbstractResourceBinderManager {

    String xsid = null;

    @Override
    public String getXsid() {
        return xsid;
    }

    @Override
    public void setXsid(String xsid) {
        this.xsid = xsid;
    }
}
