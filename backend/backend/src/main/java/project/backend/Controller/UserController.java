package project.backend.Controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import project.backend.DTO.ResponseDTO;
import project.backend.DTO.UserDTO;
import project.backend.DTO.UserTravelsDTO;
import project.backend.Entity.UserEntity;
import project.backend.Entity.UserTravelsEntity;
import project.backend.Security.TokenProvider;
import project.backend.Service.UserService;
import project.backend.Service.UserTravelsService;

import javax.naming.AuthenticationException;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Map<String, String> codeStorage = new HashMap<>(); // 전화번호와 인증 코드 저장
    private final Map<String, Long> expiryStorage = new HashMap<>(); // 코드 만료 시간 저장


    @Autowired
    private TokenProvider tokenProvider;

    private ResponseDTO responseDTO = new ResponseDTO<>();
    @Autowired
    private UserTravelsService userTravelsService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registeredUser(@RequestBody UserDTO userDTO){
        try
        {
            UserEntity user = UserEntity.builder()
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .profile(userDTO.getProfile())
                    .phone(userDTO.getPhone())
                    .build();

            UserTravelsEntity userTravels = UserTravelsEntity.builder()
                    .email(userDTO.getEmail())
                    .travelList(new ArrayList<String>())
                    .build();

            UserEntity registerUser = userService.createUser(user);

            UserTravelsEntity registerUserTravels = userTravelsService.createUserTravels(userTravels);

            UserDTO responsedUserDTO = UserDTO.builder()
                    .name(registerUser.getName())
                    .email(registerUser.getEmail())
                    .password(registerUser.getPassword())
                    .phone(registerUser.getPhone())
                    .profile(registerUser.getProfile())
                    .id(registerUser.getId())
                    .build();

            UserTravelsDTO responsedUserTravelsDTO = UserTravelsDTO.builder()
                    .id(registerUserTravels.getId())
                    .email(registerUserTravels.getEmail())
                    .travelList(registerUserTravels.getTravelList())
                    .build();

            List<Object> list = new ArrayList<>();
            list.add(responsedUserDTO);
            list.add(responsedUserTravelsDTO);
            return ResponseEntity.ok().body(responseDTO.Response("success", "우리 앱을 이용해주셔서 감사합니다. 여러분의 기입을 환영합니다.", list));

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @GetMapping("/userprofile")
    public ResponseEntity<?> UserProfile(@AuthenticationPrincipal String email) {
        System.out.println("Authenticated email: " + email);  // 이메일 값 확인

        try {
            if (email == null) {
                return ResponseEntity.badRequest().body(responseDTO.Response("error", "인증된 이메일이 없습니다."));
            }

            UserEntity user = userService.getUserInfo(email);
            System.out.println("User: " + user);  // 유저 정보 확인

            if (user == null) {
                return ResponseEntity.badRequest().body(responseDTO.Response("error", "해당 이메일의 유저를 찾을 수 없습니다."));
            }

            List<Object> list = new ArrayList<>();
            list.add(user);
            return ResponseEntity.ok().body(responseDTO.Response("success", "오늘도 저희 서비스에 방문해주셔서 감사드려요!!!", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO){
        try{
            UserEntity user = userService.getByCredentials(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);
            System.out.println(user.getEmail());
            if(user != null)
            {
                String token = tokenProvider.createToken(user);
                UserDTO responseUserDTO = UserDTO.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .phone(user.getPhone())
                        .id(user.getId())
                        .token(token)
                        .build();
                List<Object> list = new ArrayList<>();
                list.add(responseUserDTO);
                return ResponseEntity.ok().body(responseDTO.Response("success", "오늘도 저희 서비스에 방문해주셔서 감사드려요!!!", list));
            }
            else
            {
               throw new AuthenticationException("회원정보가 존재하지 않습니다. 비밀번호나 아이디를 다시 입력해주세요.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    // 수정
    @PutMapping("/edit")
    @Cacheable(value = "email", key = "#email")
    public ResponseEntity<?> editUser(@AuthenticationPrincipal String email, @RequestBody UserDTO userDTO){
        try
        {
            UserEntity user = UserEntity.builder()
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .phone(userDTO.getPhone())
                    .build();

            UserEntity editUser = userService.updateUser(email, user);

            UserDTO responsedUserDTO = UserDTO.builder()
                    .name(editUser.getName())
                    .email(editUser.getEmail())
                    .password(editUser.getPassword())
                    .phone(editUser.getPhone())
                    .build();

            List<Object> list = new ArrayList<>();
            list.add(responsedUserDTO);
            return ResponseEntity.ok().body(responseDTO.Response("info", "회원정보 수정 완료!", list));

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
    //로그아웃
    @DeleteMapping("/signout")
    @CacheEvict(value = "email")
    public ResponseEntity<?> signout(HttpServletResponse response)
    {
        // refreshToken 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null); // 빈 쿠키를 새로 생성
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 쿠키의 유효 시간을 0으로 설정, 브라우저가 즉시 이 쿠키를 삭제
        response.addCookie(refreshTokenCookie); // 수정된 쿠키(refreshTokenCookie)를 응답에 추가, 클라이언트에게 쿠키를 전송

        return ResponseEntity.ok("로그아웃 성공");
    } // 프론트엔드 연결 후 기능 정상 동작 여부 확인해야 함.


    @PostMapping("/profile")
    public ResponseEntity<?> signprofile(@RequestBody List<String> emails)
    {
        try
        {
            List<String> emailprofile = userService.getprofileByEmail(emails);
            return ResponseEntity.ok().body(responseDTO.Response("success", "회원정보 불러오기 완료!", emailprofile));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
}
