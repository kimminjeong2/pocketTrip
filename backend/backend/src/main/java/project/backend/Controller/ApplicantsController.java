package project.backend.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.backend.DTO.ApplicantsDTO;
import project.backend.DTO.ResponseDTO;
import project.backend.DTO.TravelPlanDTO;
import project.backend.Entity.ApplicantsEntity;
import project.backend.Entity.TravelPlanEntity;
import project.backend.Service.AppllicantsService;
import project.backend.Service.TravelPlanService;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/Applicants")
public class ApplicantsController
{
    private ResponseDTO responseDTO = new ResponseDTO();

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private AppllicantsService appllicantsService;

    //Travel에 데이터가 있을 때만 DB에서 insert 되게 설정
    @PostMapping("/insert/{Travelcode}")
    public ResponseEntity<?> ApplicantsInsert(@AuthenticationPrincipal String userId, @PathVariable(value = "Travelcode") String Travelcode)
    {
        try
        {

            if(travelPlanService.SelectTravelCode(Travelcode) == true)
            {
                Mono<ApplicantsEntity> applicantsEntity = appllicantsService.AppllicantsInsert(Travelcode, userId);
                Mono<ApplicantsDTO> travelPlanDTO1 = ConvertTo(applicantsEntity);
                List<Object> list = new ArrayList<>(Collections.singletonList(ConvertTo(applicantsEntity)));
                return ResponseEntity.ok().body(responseDTO.Response("success", "전송 완료", list));
            }
            else
            {
                log.warn("The data with Travelcode {} is not present in the travel document", Travelcode);
                throw new IllegalArgumentException("Travelcode 값이 없는데 넣는 것은 불가능");
                //The data for that travel code is not present in the travel document.
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
    @PostMapping("/Delete/{Travelcode}")
    public ResponseEntity<?> ApplicantsDelete(@AuthenticationPrincipal String userId, @PathVariable(value = "Travelcode") String Travelcode)
    {
        try
        {

            if(travelPlanService.SelectTravelCode(Travelcode) == true)
            {
                Mono<ApplicantsEntity> applicantsEntity = (Mono<ApplicantsEntity>) appllicantsService.AppllicantsDelete(Travelcode, userId);
                List<Object> list = new ArrayList<>(Collections.singletonList(ConvertTo(applicantsEntity)));
                return ResponseEntity.ok().body(responseDTO.Response("success", "전송 완료", list));            }
            else
            {
                log.warn("The data with Travelcode {} is not present in the travel document", Travelcode);
                throw new IllegalArgumentException("Travelcode 값이 없는데 넣는 것은 불가능");
                //The data for that travel code is not present in the travel document.
            }
        }
        catch (Exception e)
        {
            ResponseDTO<Mono<ApplicantsDTO>> responseDTO1 = responseDTO.Response("error", e.getMessage());
            return ResponseEntity.badRequest().body(responseDTO1);
        }
    }



    public Mono<ApplicantsEntity> ConvertTo(ApplicantsDTO Applicants)
    {
        ApplicantsEntity Applicantsed = ApplicantsEntity.builder()
                .travelCode(Applicants.getTravelcode())
                .userList(Applicants.getUserList())
                .id(Applicants.getId())
                .build();

        return  Mono.just(Applicantsed);
    }
    public Mono<ApplicantsDTO> ConvertTo(Mono<ApplicantsEntity> applicants)
    {
        ApplicantsDTO applicantsDTO = ApplicantsDTO.builder()
                .travelcode(applicants.block().getTravelCode())
                .userList(applicants.block().getUserList())
                .id(applicants.block().getId())
                .build();

        return  Mono.just(applicantsDTO);
    }


}
