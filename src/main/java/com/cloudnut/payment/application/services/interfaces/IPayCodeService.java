package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.paycode.PayCodeResDTO;
import com.cloudnut.payment.application.exception.BillException;
import com.cloudnut.payment.application.exception.PayCodeException;
import org.springframework.data.domain.Pageable;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface IPayCodeService {
    PayCodeResDTO generatePayCode(String token, String transactionId) throws PayCodeException.AlreadyExisted, BillException.NotFound, BillException.PaymentMethodNotSupport, UnsupportedEncodingException, MessagingException;
    PagingResponseDTO<PayCodeResDTO> getAllPayCode(String token, Pageable pageable);
    void deleteCode(String token, Long codeId);
}
