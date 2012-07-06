/*
 * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     ldoguin
 */
package org.nuxeo.preview.search.highlight;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.platform.preview.adapter.BlobPostProcessor;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 * 
 */
public class SearchKeywordBlobPostProcessor implements BlobPostProcessor {

    private static final Log log = LogFactory.getLog(SearchKeywordBlobPostProcessor.class);

    protected static final int BUFFER_SIZE = 4096 * 16;

    protected static final String SEARCH_KEYWORDS_CSS = "<link href='"
            + VirtualHostHelper.getContextPathProperty()
            + "/css/SearchKeywords.css' rel=\"stylesheet\" type=\"text/css\" />";

    protected static final String SEARCH_KEYWORDS_JS = "<script type=\"text/javascript\" src='"
            + VirtualHostHelper.getContextPathProperty()
            + "/scripts/SearchKeywords.js'></script>";

    protected static final String JQUERY_JS = "<script type=\"text/javascript\" src='"
            + "http://code.jquery.com/jquery-1.7.2.min.js'></script>";

    protected static final String INIT_SEARCHBOX_JS = "<script type=\"text/javascript\" >initSearchBox('"
            + VirtualHostHelper.getContextPathProperty() + "');</script>";

    protected Pattern headPattern = Pattern.compile("(.*)(<head>)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected Pattern htmlPattern = Pattern.compile("(.*)(<html>)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected Pattern bodyPattern = Pattern.compile(
            "(.*)(<body(?:\"[^\"]*\"['\"]*|'[^']*'['\"]*|[^'\">])+>)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected Pattern endBodyPattern = Pattern.compile("(.*)(</body>)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected Pattern charsetPattern = Pattern.compile(
            "(.*) charset=(.*?)\"(.*)", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);

    @Override
    public Blob process(Blob blob) {
        String mimetype = blob.getMimeType();
        if (mimetype == null || !mimetype.startsWith("text/")) {
            // blob does not carry HTML payload hence there is no need to try to
            // inject HTML metadata
            return blob;
        }
        try {
            String encoding = null;
            if (blob.getEncoding() == null) {
                Matcher m = charsetPattern.matcher(blob.getString());
                if (m.matches()) {
                    encoding = m.group(2);
                }
            } else {
                encoding = blob.getEncoding();
            }

            String blobAsString = getBlobAsString(blob, encoding);
            String processedBlob = addSearchKeyWordScript(blobAsString);
            processedBlob = addInitSearchBoxJS(processedBlob);

            byte[] bytes = encoding == null ? processedBlob.getBytes()
                    : processedBlob.getBytes(encoding);
            blob = new ByteArrayBlob(bytes, blob.getMimeType(), encoding);
        } catch (IOException e) {
            log.debug("Unable to process Blob", e);
        }
        return blob;
    }

    protected String getBlobAsString(Blob blob, String encoding)
            throws IOException {
        if (encoding == null) {
            return blob.getString();
        }
        Reader reader = new InputStreamReader(blob.getStream(), encoding);
        return readString(reader);
    }

    protected String addSearchKeyWordScript(String blob) {
        StringBuilder sb = new StringBuilder();
        Matcher m = headPattern.matcher(blob);
        if (m.matches()) {
            sb.append(m.group(1));
            sb.append(m.group(2));
            sb.append(SEARCH_KEYWORDS_CSS);
            sb.append(JQUERY_JS);
            sb.append(SEARCH_KEYWORDS_JS);
            sb.append(m.group(3));
        } else {
            log.debug("Unable to inject Annotation module javascript");
            sb.append(blob);
        }
        return sb.toString();
    }

    protected String addInitSearchBoxJS(String blob) {
        StringBuilder sb = new StringBuilder();
        Matcher m = endBodyPattern.matcher(blob);
        if (m.matches()) {
            sb.append(m.group(1));
            sb.append(INIT_SEARCHBOX_JS);
            sb.append(m.group(2));
            sb.append(m.group(3));
        } else {
            m = bodyPattern.matcher(blob);
            if (m.matches()) {
                sb.append(m.group(1));
                sb.append(m.group(2));
                sb.append(INIT_SEARCHBOX_JS);
                sb.append(m.group(3));
            } else {
                log.debug("Unable to inject Annotation module javascript");
                sb.append(blob);
            }
        }
        return sb.toString();
    }

    public static String readString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        try {
            char[] buffer = new char[BUFFER_SIZE];
            int read;
            while ((read = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
                sb.append(buffer, 0, read);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return sb.toString();
    }

}
