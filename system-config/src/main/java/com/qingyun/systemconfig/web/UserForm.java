package com.qingyun.systemconfig.web;

import jakarta.validation.constraints.NotBlank;

public class UserForm {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String roleCode;

    private short status = 1;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }
}