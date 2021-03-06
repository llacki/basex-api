package org.basex.test.http;

import static org.basex.http.HTTPMethod.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.test.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class contains common methods for the HTTP services.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class HTTPTest extends SandboxTest {
  /** Database context. */
  protected static final Context CONTEXT = HTTPContext.init();
  /** Start servers. */
  private static BaseXHTTP http;
  /** Root path. */
  private static String root;

  // INITIALIZATION =====================================================================

  /**
   * Initializes the test.
   * @param rt root path
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final String rt, final boolean local) throws Exception {
    initContext(CONTEXT);
    assertTrue(new IOFile(CONTEXT.mprop.get(MainProp.HTTPPATH)).md());
    root = rt;

    final StringList sl = new StringList();
    if(local) sl.add("-l");
    sl.add(new String[] { "-p9996", "-e9997", "-h9998", "-s9999", "-z",
        "-U" + Text.ADMIN, "-P" + Text.ADMIN });
    http = new BaseXHTTP(sl.toArray());
  }

  /**
   * Finish test.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    http.stop();
  }

  // PROTECTED METHODS ==================================================================

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String get(final String query) throws IOException {
    return request(query, GET);
  }

  /**
   * Executes the specified DELETE request.
   * @param query request
   * @return response code
   * @throws IOException I/O exception
   */
  protected static String delete(final String query) throws IOException {
    return request(query, DELETE);
  }

  /**
   * Executes the specified HEAD request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String head(final String query) throws IOException {
    return request(query, HEAD);
  }

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String request(final String query, final HTTPMethod method)
      throws IOException {

    final URL url = new URL(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestMethod(method.name());
      return read(new BufferInput(conn.getInputStream())).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query path
   * @param request request
   * @param type content type
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String request,
      final String type) throws IOException {

    // create connection
    final URL url = new URL(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod(POST.name());
    conn.setRequestProperty(MimeTypes.CONTENT_TYPE, type);
    // basic authentication
    final String encoded = Base64.encode(Text.ADMIN + ':' + Text.ADMIN);
    conn.setRequestProperty(HTTPText.AUTHORIZATION, HTTPText.BASIC + ' ' + encoded);
    // send query
    final OutputStream out = conn.getOutputStream();
    out.write(token(request));
    out.close();

    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Returns an exception with improved error message.
   * @param conn connection reference
   * @param ex exception
   * @return exception
   * @throws IOException I/O exception
   */
  protected static IOException error(final HttpURLConnection conn, final IOException ex)
      throws IOException {

    final String msg = read(conn.getErrorStream());
    throw new BaseXException(msg.isEmpty() ? ex.getMessage() : msg);
  }

  /**
   * Returns a string result from the specified input stream.
   * @param is input stream
   * @return string
   * @throws IOException I/O exception
   */
  protected static String read(final InputStream is) throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    if(is != null) {
      final BufferInput bi = new BufferInput(is);
      for(int i; (i = bi.read()) != -1;) ao.write(i);
      bi.close();
    }
    return ao.toString();
  }
}
