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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.User;

/** Servlet that returns comments and allows for new comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private class Comment {
      private String content;
      private long timestamp;
      private String userId;
      private String nickname;

      public Comment(String content, long timestamp, String userId, String nickname) {
          this.content = content;
          this.timestamp = timestamp;
          this.userId = userId;
          this.nickname = nickname;
      }
  }

  private String commentsToJSON(ArrayList<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<Comment> comments = new ArrayList<>();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    int num_comments = Integer.parseInt(request.getParameter("num-comments"));
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(num_comments))) {
        comments.add(new Comment((String) entity.getProperty("content"),
            (long) entity.getProperty("timestamp"),
            (String) entity.getProperty("userId"),
            (String) entity.getProperty("nickname")));
    }
    String json = commentsToJSON(comments);
    response.setContentType("text/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
        response.sendRedirect("/index.html");
        return;
    }
    String id = user.getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    Entity entity;

    // Search for the user(s) with the matching ID
    PreparedQuery results = datastore.prepare(query);
    try {
        // There should be either zero or one results, so try to treat it as one user
        entity = results.asSingleEntity();
        if (entity == null) { // Redirect if this user does not have a username registered
            // TODO: Redirect to explanatory error page
            response.sendRedirect("/index.html");
            return;
        }
    }
    catch (PreparedQuery.TooManyResultsException e) {
        // There are multiple users with this ID; end the registration
        // This is practically impossible but theoretically dangerous 
        // TODO: Redirect to explanatory error page
        response.sendRedirect("/index.html");
        return;
    }

    String nickname = (String) entity.getProperty("nickname");

    long timestamp = System.currentTimeMillis();
    String comment = request.getParameter("new-comment");

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("userId", id);
    commentEntity.setProperty("nickname", nickname);

    datastore.put(commentEntity);

    response.sendRedirect("/index.html#comments");
  }
}
