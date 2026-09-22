package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;

import java.io.IOException;
import java.net.URI;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

public class Proxy extends Module {

    public String proxyIp = "127.0.0.1";
    public int proxyPort = 9050;
    public boolean activeState = false; // Internal tracking state variable

    private final ProxySelector defaultSelector = ProxySelector.getDefault();
    private ProxySelector customSelector;

    public Proxy() {
        super("Proxy",Category.MISC);
        setupSelector();
    }

    private void setupSelector() {
        this.customSelector = new ProxySelector() {
            @Override
            public List<java.net.Proxy> select(URI uri) {
                String scheme = uri.getScheme();
                if (scheme != null && (scheme.startsWith("socket") || scheme.startsWith("tcp") || scheme.startsWith("http"))) {
                    InetSocketAddress proxyAddr = new InetSocketAddress(proxyIp, proxyPort);
                    // Explicit package declaration avoids name clashing with your class
                    return Collections.singletonList(new java.net.Proxy(java.net.Proxy.Type.SOCKS, proxyAddr));
                }
                return defaultSelector.select(uri);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                System.err.println("SoleHack Proxy connection failed: " + ioe.getMessage());
            }
        };
    }

    @Override
    public void onUpdate() {
        // Handled empty abstract method implementation required by your Module class
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.activeState = true;
        ProxySelector.setDefault(customSelector);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.activeState = false;
        ProxySelector.setDefault(defaultSelector);
    }

    public void setProxyConfig(String ip, int port) {
        this.proxyIp = ip;
        this.proxyPort = port;
        if (this.activeState) {
            ProxySelector.setDefault(customSelector);

        }
    }
}
