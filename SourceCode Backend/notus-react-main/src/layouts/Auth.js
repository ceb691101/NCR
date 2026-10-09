import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// views

import Login from "views/auth/Login.js";

export default function Auth() {
  return (
    <>
      {/* <Navbar transparent /> */}
      <main>
        <section
          className="relative w-full h-full min-h-screen"
          style={{ height: "100vh" }}
        >
          <Switch>
            <Route path="/auth/login" exact component={Login} />
            <Redirect from="/auth" to="/auth/login" />
          </Switch>
          {/* <FooterSmall absolute /> */}
        </section>
      </main>
    </>
  );
}
