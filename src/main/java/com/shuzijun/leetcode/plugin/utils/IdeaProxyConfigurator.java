package com.shuzijun.leetcode.plugin.utils;

import com.intellij.util.net.JdkProxyProvider;
import com.intellij.util.net.ProxyConfiguration;
import com.intellij.util.net.ProxyCredentialStore;
import com.intellij.util.net.ProxySettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

public class IdeaProxyConfigurator {

    public static OkHttpClient.Builder applyIdeaProxy(OkHttpClient.Builder builder) {
        ProxyConfiguration proxyConfig = ProxySettings.getInstance().getProxyConfiguration();

        if (proxyConfig instanceof ProxyConfiguration.StaticProxyConfiguration staticConfig) {
            applyStaticProxy(builder, staticConfig);
        } else {
            // PAC / Auto-detect / Direct
            // 直接使用平台推荐的方式，无需关心 IdeProxySelector 的构造参数
            builder.proxySelector(JdkProxyProvider.getInstance().getProxySelector());
        }

        builder.proxyAuthenticator(buildDynamicAuthenticator());
        return builder;
    }

    // ------------------------------------------------------------------
    // Static proxy
    // ------------------------------------------------------------------

    private static void applyStaticProxy(
            OkHttpClient.Builder builder,
            ProxyConfiguration.StaticProxyConfiguration config
    ) {
        String host = config.getHost();
        if (host.isBlank()) return;

        int port = config.getPort();
        Proxy.Type proxyType = config.getProtocol() == ProxyConfiguration.ProxyProtocol.SOCKS
                ? Proxy.Type.SOCKS : Proxy.Type.HTTP;

        String exceptions = config.getExceptions();
        if (!exceptions.isBlank()) {
            builder.proxySelector(new IdeaAwareProxySelector(host, port, proxyType, exceptions));
        } else {
            builder.proxy(new Proxy(proxyType, new InetSocketAddress(host, port)));
        }
    }

    // ------------------------------------------------------------------
    // 动态代理认证器：适用于 Static / PAC / Auto-detect 所有模式
    // PAC 下代理地址是动态的，必须从 response 的实际代理地址动态查凭据
    // ------------------------------------------------------------------

    private static Authenticator buildDynamicAuthenticator() {
        return (route, response) -> {
            // 避免认证失败后无限重试
            if (response.request().header("Proxy-Authorization") != null) {
                return null;
            }

            if (route == null || route.proxy().type() == Proxy.Type.DIRECT) {
                return null;
            }

            InetSocketAddress proxyAddress = (InetSocketAddress) route.proxy().address();
            String host = proxyAddress.getHostString();
            int port = proxyAddress.getPort();

            com.intellij.credentialStore.Credentials credentials = ProxyCredentialStore.getInstance().getCredentials(host, port);
            if (credentials == null) return null;

            String username = credentials.getUserName();
            String password = credentials.getPasswordAsString();
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                return null;
            }

            return response.request().newBuilder()
                    .header("Proxy-Authorization", Credentials.basic(username, password))
                    .build();
        };
    }

    // ------------------------------------------------------------------
    // no-proxy 例外支持
    // ------------------------------------------------------------------

    static class IdeaAwareProxySelector extends ProxySelector {

        private final Proxy proxy;
        private final List<String> noProxyHosts;

        IdeaAwareProxySelector(String host, int port, Proxy.Type type, String exceptions) {
            this.proxy = new Proxy(type, new InetSocketAddress(host, port));
            this.noProxyHosts = Arrays.stream(exceptions.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        @Override
        public List<Proxy> select(URI uri) {
            String host = uri.getHost();
            if (host == null) return List.of(proxy);

            boolean excluded = noProxyHosts.stream().anyMatch(exception -> {
                if (exception.startsWith("*")) {
                    return host.endsWith(exception.substring(1));
                }
                return host.equalsIgnoreCase(exception);
            });

            return excluded ? List.of(Proxy.NO_PROXY) : List.of(proxy);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            // 可在此记录日志
        }
    }
}