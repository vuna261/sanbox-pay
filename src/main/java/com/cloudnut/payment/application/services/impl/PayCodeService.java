package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.paycode.PayCodeResDTO;
import com.cloudnut.payment.application.exception.BillException;
import com.cloudnut.payment.application.exception.PayCodeException;
import com.cloudnut.payment.application.services.interfaces.IPayCodeService;
import com.cloudnut.payment.infrastructure.entity.PayCodeEntityDB;
import com.cloudnut.payment.infrastructure.entity.RechargeEntityDB;
import com.cloudnut.payment.infrastructure.repository.PayCodeEntityRepo;
import com.cloudnut.payment.infrastructure.repository.RechargeEntityRepo;
import com.cloudnut.payment.utils.PaymentUtils;
import com.cloudnut.payment.utils.RoleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PayCodeService implements IPayCodeService {

    @Value("${mail.send_email}")
    private String MAIL_SENDER;

    @Value("${mail.send_name}")
    private String MAIL_SENDER_NAME;

    @Value("${mail.send_subject}")
    private String MAIL_SENDER_SUBJECT;

    @Value("${mail.send_content}")
    private String MAIL_SENDER_CONTENT;

    @Autowired
    private PayCodeEntityRepo payCodeEntityRepo;

    @Autowired
    private RechargeEntityRepo rechargeEntityRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * generate paycode
     * @param token
     * @param transactionId
     * @return
     * @throws PayCodeException.AlreadyExisted
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PayCodeResDTO generatePayCode(String token, String transactionId) throws
            PayCodeException.AlreadyExisted, BillException.NotFound,
            BillException.PaymentMethodNotSupport, UnsupportedEncodingException, MessagingException {
        Optional<PayCodeEntityDB> entityDBOptional =
                payCodeEntityRepo.findByTransactionId(transactionId);
        if (entityDBOptional.isPresent()) {
            throw new PayCodeException.AlreadyExisted();
        }

        // get bill information
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);
        if (!rechargeEntityDBOptional.isPresent() ||
                (rechargeEntityDBOptional.get().getBillStatus() != PaymentUtils.BILL_STATUS.Initial
                && rechargeEntityDBOptional.get().getBillStatus() != PaymentUtils.BILL_STATUS.Pending)) {
            throw new BillException.NotFound();
        }

        RechargeEntityDB rechargeEntityDB = rechargeEntityDBOptional.get();
        if (rechargeEntityDB.getPaymentMethod() != PaymentUtils.PAYMENT_METHOD.CODE) {
            throw new BillException.PaymentMethodNotSupport();
        }

        String payCode = PaymentUtils.generatePayCode(transactionId);
        PayCodeEntityDB payCodeEntityDB = PayCodeEntityDB.builder()
                .createdDate(new Date())
                .payCode(payCode)
                .transactionId(transactionId)
                .used(false)
                .build();
        payCodeEntityDB = payCodeEntityRepo.save(payCodeEntityDB);
        sendEmail(rechargeEntityDB.getEmail(), payCode);
        return PayCodeResDTO.from(payCodeEntityDB);
    }

    /**
     * get all pay code
     * @param token
     * @param pageable
     * @return
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PagingResponseDTO<PayCodeResDTO> getAllPayCode(String token, Pageable pageable) {
        Page<PayCodeEntityDB> payCodeEntityDBPage =
                payCodeEntityRepo.findAll(pageable);
        List<PayCodeEntityDB> payCodeEntityDBS = payCodeEntityDBPage.getContent();
        List<PayCodeResDTO> payCodeResDTOS = payCodeEntityDBS.stream()
                .map(PayCodeResDTO::from)
                .collect(Collectors.toList());
        return PagingResponseDTO.from(payCodeResDTOS, payCodeEntityDBPage.getTotalPages(),
                payCodeEntityDBPage.getTotalElements());
    }

    /**
     * delete paycode
     * @param token
     * @param codeId
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public void deleteCode(String token, Long codeId) {
        payCodeEntityRepo.deleteById(codeId);
    }

    /**
     * send code via email
     * @param email
     * @param payCode
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    private void sendEmail(String email, String payCode) throws
            UnsupportedEncodingException, MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(MAIL_SENDER, MAIL_SENDER_NAME);
        helper.setTo(email);
        helper.setSubject(MAIL_SENDER_SUBJECT);

        String content = MAIL_SENDER_CONTENT.replace("[[payCode]]", payCode);
        helper.setText(content, true);
        javaMailSender.send(message);
    }
}
