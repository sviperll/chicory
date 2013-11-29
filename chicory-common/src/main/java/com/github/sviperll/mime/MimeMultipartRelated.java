/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.mime;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;

public class MimeMultipartRelated extends MimeMultipart {
    private final ContentType baseContentTypeObject;
    private String rootId = null;

    public MimeMultipartRelated() {
        super("related");
        try {
            baseContentTypeObject = new ContentType(contentType);
        } catch (ParseException ex) {
            throw new RuntimeException("\"multipart/related\" string should be considered well-formed content type", ex);
        }
    }

    @Override
    public void addBodyPart(BodyPart part) throws MessagingException {
        super.addBodyPart(part);
        updateContentType();
    }

    @Override
    public void addBodyPart(BodyPart part, int index) throws MessagingException {
        super.addBodyPart(part, index);
        updateContentType();
    }

    @Override
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        boolean res = super.removeBodyPart(part);
        updateContentType(true);
        return res;
    }

    @Override
    public void removeBodyPart(int index) throws MessagingException {
        super.removeBodyPart(index);
        updateContentType(true);
    }

    public void setRoot(String contentId) throws MessagingException {
        rootId = contentId;
        updateContentType();
    }

    public void useDefaultRoot() throws MessagingException {
        rootId = null;
        updateContentType();
    }

    private void updateContentType(boolean cleanDefaultRoot) throws MessagingException {
        BodyPart part = null;
        if (rootId == null) {
            part = getBodyPart(0);
        } else {
            part = getBodyPart(rootId);
            if (part == null) {
                if (cleanDefaultRoot)
                    rootId = null;
                else
                    throw new MessagingException("Can not set root: " + rootId + ": not found");
            }
        }
        if (part != null) {
            String primaryType = baseContentTypeObject.getPrimaryType();
            String subType = baseContentTypeObject.getSubType();
            ParameterList params = baseContentTypeObject.getParameterList();
            ContentType newContentType = new ContentType(primaryType, subType, params);
            ContentType rootContentType = new ContentType(part.getDataHandler().getContentType());
            newContentType.setParameter("type", rootContentType.getBaseType());
            if (rootId != null)
                newContentType.setParameter("start", rootId);
            contentType = newContentType.toString();
        }
    }

    private void updateContentType() throws MessagingException {
        updateContentType(false);
    }
}
