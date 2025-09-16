//package za.co.fintrack.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import za.co.fintrack.mappers.Mapper;
//import za.co.fintrack.models.entities.User;
//import za.co.fintrack.models.dtos.UserDto;
//import za.co.fintrack.services.UserService;
//
//@RestController
//@RequestMapping(path = "api/v1/auth/user")
//public class UserController {
//
//
//    private final UserService userService;
//
//    private final Mapper<User, UserDto> userDtoMapper;
//
//    @Autowired
//    public UserController(UserService userService, Mapper<User, UserDto> userDtoMapper) {
//        this.userService = userService;
//        this.userDtoMapper = userDtoMapper;
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto user) {
//        User userEntity = userDtoMapper.mapFrom(user);
//        User savedUserEntity = userService.saveUser(userEntity);
//        return new ResponseEntity<>(userDtoMapper.mapTo(savedUserEntity), HttpStatus.CREATED);
//    }
//
//}
