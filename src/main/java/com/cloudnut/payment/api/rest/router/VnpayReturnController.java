package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.application.dto.response.wallet.HandlerVnpayResDTO;
import com.cloudnut.payment.application.services.interfaces.IRechargeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("${app.base-url}")
@Slf4j
public class VnpayReturnController {

    @Value("${vnpay.pay.client-return}")
    private String VNPAY_RETURN_URL;

    @Autowired
    private IRechargeService rechargeService;


    @GetMapping("/vnp-return")
    public void handlerPaySuccess(
            HttpServletResponse response,
            @RequestParam HashMap<String, String> params
    ) throws IOException {
        HandlerVnpayResDTO isFailure = HandlerVnpayResDTO.builder().build();
       try {
           isFailure = rechargeService.handlerVnPay(params);
       } catch (Exception e) {
           log.error(e.getMessage(), e);
       }
       response.sendRedirect(VNPAY_RETURN_URL + "?transactionId=" + isFailure.getTransactionId() + "&status=" + isFailure.getStatus());
    }
}
