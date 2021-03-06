package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.data.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class retrieves resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RESTRetrieve extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRetrieve(final String in, final Map<String, String[]> vars,
      final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    // open addressed database
    open(http);

    final Session session = http.session();
    if(http.depth() == 0) {
      // list databases
      final Table table = new Table(session.execute(new List()));
      final SerializerProp sprop = new SerializerProp(http.serialization);
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sprop);
      http.initResponse(sprop);

      final FElem el = new FElem(DATABASES, new Atts(REST, RESTURI));
      el.add(new FAttr(RESOURCES, token(table.contents.size())));
      list(table, el, DATABASE, 1);
      ser.serialize(el);
      ser.close();
    } else if(!exists(http)) {
      // list database resources
      final Table table = new Table(session.execute(new ListDB(http.path())));
      if(table.contents.isEmpty()) HTTPErr.UNKNOWN_PATH.thrw();

      final String serial = http.serialization;
      final SerializerProp sprop = new SerializerProp(serial);
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sprop);
      http.initResponse(sprop);

      final FElem el = new FElem(DATABASE, new Atts(REST, RESTURI));
      el.add(new FAttr(DataText.T_NAME, token(http.db())));
      el.add(new FAttr(RESOURCES, token(table.contents.size())));
      list(table, el, RESOURCE, 0);
      ser.serialize(el);
      ser.close();
    } else if(isRaw(http)) {
      // retrieve raw file; prefix user parameters with media type
      final String ct = SerializerProp.S_MEDIA_TYPE[0] + "=" + contentType(http);
      http.initResponse(new SerializerProp(ct + ',' + http.serialization));
      session.setOutputStream(http.res.getOutputStream());
      session.execute(new Retrieve(http.dbpath()));
    } else {
      // retrieve xml file
      http.initResponse(new SerializerProp(http.serialization));
      session.execute(new Set(Prop.SERIALIZER, serial(http)));
      session.setOutputStream(http.res.getOutputStream());
      session.query(".").execute();
    }
  }

  /**
   * Lists the table contents.
   * @param table table reference
   * @param root root node
   * @param header table header
   * @param skip number of columns to skip
   */
  private static void list(final Table table, final FElem root,
      final byte[] header, final int skip) {

    for(final TokenList l : table.contents) {
      final FElem el = new FElem(header);
      // don't show last attribute (input path)
      for(int i = 1; i < l.size() - skip; i++) {
        el.add(new FAttr(lc(table.header.get(i)), l.get(i)));
      }
      el.add(new FTxt(l.get(0)));
      root.add(el);
    }
  }
}
