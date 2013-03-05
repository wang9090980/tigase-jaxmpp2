package tigase.jaxmpp.core.client.xmpp.modules.bind;

import tigase.jaxmpp.core.client.xml.Element;

/**
 * Created with IntelliJ IDEA.
 * User: wangkang
 * Date: 13-3-5
 * Time: 下午12:51
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResourceBinderManager {

    public void initialize() {
    }

    public abstract void handleBindRequestElement(Element bind);

    public abstract void handleBindResultElement(Element bind);

    public abstract void setKickFlag(Boolean isKick);
}
