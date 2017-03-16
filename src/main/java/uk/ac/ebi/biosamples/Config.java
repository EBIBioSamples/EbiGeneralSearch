package uk.ac.ebi.biosamples;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@Configuration
public class Config {

    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 10;

    @Value("${resource.retrieve.connection.timeout:10}")
    int connectionTimeout;

    @Bean
    MappingJackson2HttpMessageConverter createHalConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);

        return converter;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        builder = builder.requestFactory(httpRequestFactory());
        builder = builder.additionalMessageConverters(createHalConverter());
        return builder.build();

    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public HttpClient httpClient() {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout * 1000)
                .setConnectionRequestTimeout(connectionTimeout * 1000)
                .setSocketTimeout(connectionTimeout * 1000).build();

        HttpClient defaultHttpClient = HttpClientBuilder.create()
//                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .build();

        return defaultHttpClient;
    }

}
