/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.DataHolder;
import tigase.jaxmpp.core.client.SessionObject;

/**
 * Roster item. Contains information about buddy in roster.
 */
public class RosterItem {

	/**
	 * Susbcription state.
	 */
	public static enum Subscription {
		/**
		 * The user and the contact have subscriptions to each other's presence
		 * (also called a "mutual subscription").
		 */
		both(true, true),
		/**
		 * The contact has a subscription to the user's presence, but the user
		 * does not have a subscription to the contact's presence.
		 */
		from(true, false),
		/**
		 * The user does not have a subscription to the contact's presence, and
		 * the contact does not have a subscription to the user's presence.
		 */
		none(false, false),
		remove(false, false),
		/**
		 * The user has a subscription to the contact's presence, but the
		 * contact does not have a subscription to the user's presence.
		 */
		to(false, true);

		private final boolean sFrom;

		private final boolean sTo;

		private Subscription(boolean statusFrom, boolean statusTo) {
			this.sFrom = statusFrom;
			this.sTo = statusTo;
		}

		public boolean isFrom() {
			return this.sFrom;
		}

		public boolean isTo() {
			return this.sTo;
		}
	}

	public static final String ID_KEY = "id";

	private boolean ask;

	private final DataHolder dataHolder = new DataHolder();

	private final ArrayList<String> groups = new ArrayList<String>();

	private final BareJID jid;

	private String name;

	private final SessionObject sessionObject;

	private Subscription subscription;

	private String category;

	public RosterItem(BareJID jid, SessionObject sessionObject) {
		this.jid = jid;
		this.sessionObject = sessionObject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RosterItem))
			return false;
		return ((RosterItem) obj).jid.equals(this.jid);
	}

	/**
	 * Returns object stored by {@linkplain RosterItem#setData(String, Object)}.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return object or <code>null</code> if object doesn't exists.
	 */
	public <T> T getData(String key) {
		return dataHolder.getData(key);
	}

	/**
	 * Returns groups associated to roster item.
	 * 
	 * @return
	 */
	public ArrayList<String> getGroups() {
		return groups;
	}

	/**
	 * Returns internal RosterItem object ID.
	 * 
	 * @return object id.
	 */
	public long getId() {
		Long x = getData(ID_KEY);
		return x == null ? 0 : x;
	}

	/**
	 * Returns JID of buddy.
	 * 
	 * @return JID of buddy.
	 */
	public BareJID getJid() {
		return jid;
	}

	/**
	 * Returns name of buddy.
	 * 
	 * @return name of buddy.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns session object related to roster item.
	 * 
	 * @return session object.
	 */
	public SessionObject getSessionObject() {
		return sessionObject;
	}

	/**
	 * Returns subscription state.
	 * 
	 * @return subscription state.
	 */
	public Subscription getSubscription() {
		return subscription;
	}

    /**
     * Returns category of buddy.
     *
     * @return category of buddy.
     */
    public String getCategory() {
        return category;
    }

	@Override
	public int hashCode() {
		return jid.hashCode();
	}

	/**
	 * Checks if subscription was requested.
	 * 
	 * @return <code>true</code> subscription of this buddy was requested.
	 */
	public boolean isAsk() {
		return ask;
	}

	/**
	 * Removes data stored by {@linkplain RosterItem#setData(String, Object)}.
	 * 
	 * @param key
	 *            the key whose associated value is to be removed
	 * @return removed value or <code>null</code> if value was not saved.
	 */
	public <T> T removeData(String key) {
		return dataHolder.removeData(key);
	}

	public void setAsk(boolean ask) {
		this.ask = ask;
	}

	/**
	 * Store object in roster item. Object will not be stored on server or in
	 * local cache.
	 * 
	 * @param key
	 *            the key whose associated value is to be saved
	 * @param value
	 *            values to save
	 */
	public void setData(String key, Object value) {
		dataHolder.setData(key, value);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

    public void setCategory(String category) {
        this.category = category;
    }

	@Override
	public String toString() {
		return "RosterItem [" + name + " <" + jid.toString() + ">]";
	}

}