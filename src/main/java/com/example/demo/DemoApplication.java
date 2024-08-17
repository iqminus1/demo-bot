package com.example.demo;

import com.example.demo.service.BotSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.util.Random;

@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public BotSender botSender() {
        return new BotSender(new DefaultBotOptions(), "7320858493:AAFtIOr8bofMTKFuMjegu8SVuxxrdTMYagI");
    }

    @Bean
    public Random random() {
        return new Random();
    }
}
