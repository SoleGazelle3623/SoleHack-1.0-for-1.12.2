package com.yourwebsitespace.solehack.api;

import com.yourwebsitespace.solehack.Module;
import java.util.List;

public interface ISoleHackAddon {
    String getName();
    String getAuthor();
    List<Module> getModules();
}