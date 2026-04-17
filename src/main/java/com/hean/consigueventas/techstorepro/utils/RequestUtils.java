package com.hean.consigueventas.techstorepro.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();// Primero validamos la Ip que Spring ya debio verificar
        if ("0:0:0:0:0:0:0:1".equals(ip)) { return "127.0.0.1"; } // Si devuelve la IPv6 local (0:0:0:0:0:0:0:1), es el localhost
        return ip;
    }
}