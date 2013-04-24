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
package tigase.jaxmpp.core.client.xmpp.modules.vcard;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.io.Serializable;
import java.util.List;

public class VCard implements Serializable {

    private static final long serialVersionUID = 1L;
    private String fullname;
    private String email;
    private String phone;
    private String interphone;
    private String gender;
    private String orgName;
    private String orgId;
    private String photoType;
    private String photoVal;
    private String photoUrl;

    private static void add(Element vcard, String name, String value) throws XMLException {
        if (value != null)
            vcard.addChild(new DefaultElement(name, value, null));
    }

    private static void add(Element vcard, String name, String[] childNames, String[] values) throws XMLException {
        Element x = new DefaultElement(name);
        vcard.addChild(x);

        for (int i = 0; i < childNames.length; i++) {
            x.addChild(new DefaultElement(childNames[i], values[i], null));
        }

    }

    private static String getChildValue(Element it, String string) throws XMLException {
        List<Element> l = it.getChildren(string);
        if (l == null || l.size() == 0)
            return null;
        return l.get(0).getValue();
    }

    private static boolean match(final Element it, final String elemName, final String... children) throws XMLException {
        if (!elemName.equals(it.getName()))
            return false;

        for (String string : children) {
            List<Element> l = it.getChildren(string);
            if (l == null || l.size() == 0)
                return false;
        }

        return true;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInterphone() {
        return interphone;
    }

    public void setInterphone(String interphone) {
        this.interphone = interphone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public String getPhotoVal() {
        return photoVal;
    }

    public void setPhotoVal(String photoVal) {
        this.photoVal = photoVal;
    }

    void loadData(final Element element) throws XMLException {
        if (!element.getName().equals("vCard") || !element.getXMLNS().equals("vcard-temp"))
            throw new RuntimeException("Element isn't correct <vCard xmlns='vcard-temp'> vcard element");
        for (final Element it : element.getChildren()) {
            if ("FN".equals(it.getName())) {
                this.fullname = it.getValue();
            } else if ("gender".equals(it.getName())) {
                this.gender = it.getValue();
            } else if ("orgId".equals(it.getName())) {
                this.orgId = it.getValue();
            } else if ("orgName".equals(it.getName())) {
                this.orgName = it.getValue();
            } else if ("email".equals(it.getName())) {
                this.email = it.getValue();
            } else if ("phone".equals(it.getName())) {
                this.phone = it.getValue();
            } else if ("interphone".equals(it.getName())) {
                this.interphone = it.getValue();
            } else if ("PHOTO".equals(it.getName())) {
                for (Element pit : it.getChildren()) {
                    if ("TYPE".equals(pit.getName())) {
                        this.photoType = pit.getValue();
                    } else if ("BINVAL".equals(pit.getName())) {
                        this.photoVal = pit.getValue();
                        if (this.photoVal != null)
                            this.photoVal = this.photoVal.replace("\n", "").trim();
                    } else if ("url".equals(pit.getName())) {
                        this.photoUrl = pit.getValue();
                    }
                }
            }

        }
    }

    public Element makeElement() throws XMLException {
        Element vcard = new DefaultElement("vCard", null, "vcard-temp");
        add(vcard, "FN", this.fullname);
        add(vcard, "email", this.email);
        add(vcard, "phone", this.phone);
        add(vcard, "interphone", this.interphone);
        add(vcard, "gender", this.gender);
        add(vcard, "orgId", this.orgId);
        add(vcard, "orgName", this.orgName);
        add(vcard, "PHOTO", new String[]{"TYPE", "BINVAL", "url"}, new String[]{this.photoType, this.photoVal, this.photoUrl});
        return vcard;
    }
}