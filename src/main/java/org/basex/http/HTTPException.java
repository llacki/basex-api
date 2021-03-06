package org.basex.http;

import org.basex.util.*;

/**
 * HTTP exception. Also thrown to pass on correct status codes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPException extends Exception {
  /** Status code. */
  private final int status;

  /**
   * Constructs an exception with the specified message and extension.
   * @param err error
   * @param ext message extension
   */
  public HTTPException(final HTTPErr err, final Object... ext) {
    //this(err.code, err.desc, ext);
    super(Util.info(err.desc, ext));
    status = err.code;
  }

  /**
   * Returns the status code.
   * @return status code
   */
  public int getStatus() {
    return status;
  }
}
