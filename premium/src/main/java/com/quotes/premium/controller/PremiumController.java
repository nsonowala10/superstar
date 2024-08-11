//package com.quotes.premium.controller;
//
//import com.quotes.premium.dto.AmountDivision;
//import com.quotes.premium.dto.ApiResponse;
//import com.quotes.premium.dto.PremiumRequest;
//import com.quotes.premium.service.PremiumService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RequestMapping("/quote")
//@RestController("use for premium calculation of superstar product")
//public class PremiumController {
//
//    @Autowired
//    private PremiumService premiumService;
//
//   @PostMapping
//    public ResponseEntity<ApiResponse> fetchPremium(@RequestBody PremiumRequest premiumRequest){
//       try {
//           return ResponseEntity.ok().body(ApiResponse.buildResponse(this.premiumService.calculatePremium(premiumRequest),"success",true));
//       } catch (Exception e) {
//           return ResponseEntity.ok().body(ApiResponse.buildResponse(null,e.getMessage(),false));
//       }
//   }
//}
