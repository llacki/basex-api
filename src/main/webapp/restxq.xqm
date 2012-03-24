(:~
 : This module contains some basic examples for RESTXQ annotations
 : @author BaseX Team
 :)
module namespace page = 'http://basex.org/modules/web-page';

declare namespace rest = 'http://exquery.org/ns/restxq';

declare %rest:path("")
        %output:method("xhtml")
        %output:omit-xml-declaration("no")
        %output:doctype-public("-//W3C//DTD XHTML 1.0 Transitional//EN")
        %output:doctype-system("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd")
  function page:start() {

  let $title := 'Welcome to the RESTXQ Service' return
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>{ $title }</title>
      <link rel="stylesheet" type="text/css" href="/style.css"/>
    </head>
    <body>
      <div style="float:right"><img src="/basex.svg" width="75"/></div>
      <h2>{ $title }</h2>
      <p><a href="http://docs.basex.org/wiki/RESTXQ">RESTXQ</a> is a new API
      that facilitates the use of XQuery as a Server Side processing language
      for the Web.<br/>
      This page has been generated by a RESTXQ module, located in the web
      server's root directory.</p>
      <p>The following links return dynamic pages generated by the same code:</p>
      <ul>
        <li><a href="hello/World">hello/World</a></li>
        <li><a href="hello/Universe">hello/Universe</a></li>
      </ul>
      <p>The next example processes a simple form via POST:</p>
      <form method="post" action="form">
        <p>Your message:<br />
        <input name="content" size="50"></input>
        <input type="submit" /></p>
      </form>
      <p>The source of this file is shown below:</p>
      <hr/>
      <pre>{ unparsed-text(static-base-uri()) }</pre>
      <hr/>
      <p style='text-align:right;'><a href='..'>...back to main page</a></p>
    </body>
  </html>
};

declare %rest:path("hello/{$world}")
        %rest:GET
        %rest:header-param("User-Agent", "{$agent}")
        function page:hello($world as xs:string, $agent as xs:string*) {
  <response>
    <title>Hello { $world }!</title>
    <info>You requested this page with { $agent }.</info>
    <time>The current time is: { current-time() }</time>
  </response>
};

declare %rest:path("form/")
        %rest:POST
        %rest:form-param("content","{$message}", "'no message delivered'")
        function page:hello-postman($message as xs:string) {
  <response>
    <title>Hello!</title>
    <info>It seems you posted a message: { $message }</info>
  </response>
};
