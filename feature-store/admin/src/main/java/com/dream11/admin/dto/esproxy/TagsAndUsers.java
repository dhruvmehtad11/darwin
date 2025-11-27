package com.dream11.admin.dto.esproxy;

import java.util.List;

public class TagsAndUsers {
  private final List<String> tags;
  private final List<String> users;

  public TagsAndUsers(List<String> tags, List<String> users) {
    this.tags = tags;
    this.users = users;
  }

  public List<String> getTags() {
    return tags;
  }

  public List<String> getUsers() {
    return users;
  }
}
