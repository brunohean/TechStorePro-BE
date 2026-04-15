package com.hean.consigueventas.techstorepro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Este es el interruptor que activa las tareas en segundo plano
public class SchedulingConfig {
}
