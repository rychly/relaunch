package com.harasoft.relaunch.Utils;

/**
 * Created by anat on 18.11.17.
 * Информация для подключения и работы и OPDS каталогом
 */
public class InfoConnectOPDS {
    // данные из базы
    private int id;
    private String title;
    private String link;
    private boolean enable_pass;
    private String login;
    private String password;
    private boolean enable_proxy;
    private int proxy_type;
    private String proxy_name;
    private int proxy_port;

    //--------------------------
    // получаем данные из базы
    public void setId(String id) {
        this.id = Integer.parseInt(id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setEnable_pass(String enable_pass) {
        this.enable_pass = (enable_pass.equals("true"));
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnable_proxy(String enable_proxy) {
        this.enable_proxy = (enable_proxy.equals("true"));
    }

    public void setProxy_type(String proxy_type) {
        if (!proxy_type.trim().equals("0") && !proxy_type.trim().equals("1") && !proxy_type.trim().equals("2")) {
            this.proxy_type = 0;
        }else {
            this.proxy_type = Integer.parseInt(proxy_type);
        }
    }

    public void setProxy_name(String proxy_name) {
        this.proxy_name = proxy_name;
    }

    public void setProxy_port(String proxy_port) {
        try {
            this.proxy_port = Integer.parseInt(proxy_port);
        } catch (NumberFormatException e) {
            this.proxy_port = 8118;
        }
    }
    //=========================
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public boolean isEnable_pass() {
        return enable_pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnable_proxy() {
        return enable_proxy;
    }

    public int getProxy_type() {
        return proxy_type;
    }

    public String getProxy_name() {
        return proxy_name;
    }

    public int getProxy_port() {
        return proxy_port;
    }

}
