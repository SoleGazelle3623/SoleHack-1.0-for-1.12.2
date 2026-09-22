package com.yourwebsitespace.solehack;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {

    private final List<String> friends = new ArrayList<>();
    public static FriendManager INSTANCE;

    public FriendManager() {
        INSTANCE = this;
    }

    public void addFriend(String name) {
        if (!isFriend(name)) {
            friends.add(name);
        }
    }

    public void removeFriend(String name) {
        friends.removeIf(friend -> friend.equalsIgnoreCase(name));
    }

    public boolean isFriend(String name) {
        for (String friend : friends) {
            if (friend.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getFriends() {
        return friends;
    }
}