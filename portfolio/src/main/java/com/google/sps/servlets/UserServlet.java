// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that handles user authentication. */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

  private class LoginStatus {
    @SerializedName("logged_in")
    private boolean logged_in;

    @SerializedName("url")
    private String url;

    public LoginStatus(boolean logged_in, String url) {
      this.logged_in = logged_in;
      this.url = url;
    }
  }

  String makeJSONStatus(boolean logged_in, String url) {
    Gson gson = new Gson();
    String json = gson.toJson(new LoginStatus(logged_in, url));
    return json;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    boolean logged_in = userService.isUserLoggedIn();
    String url = (logged_in)
        ? userService.createLogoutURL("/")
        : userService.createLoginURL("/");

    response.setContentType("text/json;");
    response.getWriter().println(makeJSONStatus(logged_in, url));
  }
}
