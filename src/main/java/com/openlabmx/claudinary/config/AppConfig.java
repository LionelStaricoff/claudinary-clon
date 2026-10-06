package com.openlabmx.claudinary.config;

import com.openlabmx.claudinary.dto.response.UserResponse;
import com.openlabmx.claudinary.entity.Role;
import com.openlabmx.claudinary.entity.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
              .setDeepMapEnabled(false)
              .setFieldMatchingEnabled(true)
              .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        TypeMap<User, UserResponse> userMap = mapper.createTypeMap(User.class, UserResponse.class);
        userMap.<Set<String>, User>addMapping(
            src -> src.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet()),
            UserResponse::setRoles);
        return mapper;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", "classpath:uploads/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/templates/images/", "file:uploads/");
    }
}
