package com.efcon.ride.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    @Bean
    @Qualifier("rideNotificationQueue")
    public Queue rideNotificationQueue(@Value("${mq.ride-notifications.send.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    @Qualifier("rideNotificationExchange")
    public FanoutExchange rideNotificationExchange(@Value("${mq.ride-notifications.send.exchange}") String exchangeName) {
        return new FanoutExchange(exchangeName, true, false);
    }

    @Bean
    public Binding rideNotificationBinding(@Qualifier("rideNotificationExchange") FanoutExchange fanoutExchange,
                                           @Qualifier("rideNotificationQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
