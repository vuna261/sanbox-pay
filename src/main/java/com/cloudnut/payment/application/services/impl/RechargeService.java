package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.request.recharge.PayReqDTO;
import com.cloudnut.payment.application.dto.request.recharge.RechargeReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.recharge.RechargeResDTO;
import com.cloudnut.payment.application.dto.response.wallet.HandlerVnpayResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BillException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.exception.PromoException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.application.services.interfaces.IPromoService;
import com.cloudnut.payment.application.services.interfaces.IRechargeService;
import com.cloudnut.payment.infrastructure.entity.*;
import com.cloudnut.payment.infrastructure.repository.*;
import com.cloudnut.payment.utils.Constants;
import com.cloudnut.payment.utils.PaymentUtils;
import com.cloudnut.payment.utils.VnpayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class RechargeService implements IRechargeService {
    @Value("${vnpay.url}")
    private String VNPAY_URL;

    @Value("${vnpay.pay.path}")
    private String VNPAY_PAY_PATH;

    @Value("${vnpay.client-id}")
    private String VNPAY_CLIENT_ID;

    @Value("${vnpay.client-secret}")
    private String VNPAY_SECRET;

    @Value("${vnpay.pay.return-url}")
    private String VNPAY_RETURN_URL;

    @Autowired
    private IAuthService authService;

    @Autowired
    private PayCodeEntityRepo payCodeEntityRepo;

    @Autowired
    private IPromoService promoService;

    @Autowired
    private PromoEntityRepo promoEntityRepo;

    @Autowired
    private PromoHistoryEntityRepo promoHistoryEntityRepo;

    @Autowired
    private PriceEntityRepo priceEntityRepo;

    @Autowired
    private TransactionHistoryEntityRepo transactionHistoryEntityRepo;

    @Autowired
    private RechargeEntityRepo rechargeEntityRepo;

    @Autowired
    private WalletEntityRepo walletEntityRepo;

    /**
     * create new bill
     * @param token
     * @param rechargeReqDTO
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws PriceException.NotFound
     * @throws BillException.NotCompleted
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public RechargeResDTO createBill(String token, RechargeReqDTO rechargeReqDTO)
            throws AuthenticationException.MissingToken, PriceException.NotFound, BillException.NotCompleted {
        Long userId = authService.getUserId(token);
        String email = authService.getEmail(token);

        // step 0: check bill in process
        Optional<RechargeEntityDB> optionalRechargeEntityDB =
                rechargeEntityRepo.findByUserIdAndBillStatus(userId, PaymentUtils.BILL_STATUS.Initial);

        Optional<RechargeEntityDB> optionalRechargeEntityDB1 =
                rechargeEntityRepo.findByUserIdAndBillStatus(userId, PaymentUtils.BILL_STATUS.Pending);

        if (optionalRechargeEntityDB.isPresent() || optionalRechargeEntityDB1.isPresent()) {
            throw new BillException.NotCompleted();
        }

        // step 1: get detail of bought item
        PriceEntityDB priceEntityDB = getPrice(rechargeReqDTO.getPriceId());

        // get apply promote code

        // step 2: (Optional) get detail of promote code
        Long newPrice = getNewPrice(priceEntityDB, null);

        // step 3: init payment
        RechargeEntityDB rechargeEntityDB = RechargeEntityDB.builder()
                .amount(newPrice)
                .billStatus(PaymentUtils.BILL_STATUS.Initial)
                .paymentMethod(PaymentUtils.PAYMENT_METHOD.CODE)
                .userId(userId)
                .email((rechargeReqDTO.getEmail() == null
                        || rechargeReqDTO.getEmail().isEmpty()) ? email : rechargeReqDTO.getEmail())
                .priceId(priceEntityDB.getId())
                .description(Constants.PAY_DESCRIPTION.replace("[[transactionId]]", priceEntityDB.getName()))
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();

        rechargeEntityDB = rechargeEntityRepo.save(rechargeEntityDB);

        RechargeResDTO rechargeResDTO = RechargeResDTO.from(rechargeEntityDB);
        rechargeResDTO.setItemId(priceEntityDB.getId());
        rechargeResDTO.setItemName(priceEntityDB.getName());
        rechargeResDTO.setItemAmount(priceEntityDB.getAmount());
        rechargeResDTO.setItemPrice(priceEntityDB.getPrice());
        rechargeResDTO.setPriceName(priceEntityDB.getName());
        rechargeResDTO.setPromoCode(rechargeReqDTO.getPromoCode());

        return rechargeResDTO;
    }

    /**
     * update bill
     * @param token
     * @param transactionId
     * @param rechargeReqDTO
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws PriceException.NotFound
     * @throws BillException.NeedCancelFirst
     * @throws BillException.NotFound
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public RechargeResDTO updateBill(String token, String transactionId, RechargeReqDTO rechargeReqDTO)
            throws AuthenticationException.MissingToken,
            PriceException.NotFound, BillException.NeedCancelFirst,
            BillException.NotFound, PromoException.OutOfTotal, PromoException.NotAvailable {
        // only update for initial status
        Long userId = authService.getUserId(token);
        String email = authService.getEmail(token);

        // step 1: check payment has existed or not
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);
        if (!rechargeEntityDBOptional.isPresent()) {
            throw new PriceException.NotFound();
        }

        // step 2: if existed -> need check can update
        RechargeEntityDB rechargeEntityDB = rechargeEntityDBOptional.get();
        if (rechargeEntityDB.getBillStatus() == PaymentUtils.BILL_STATUS.Pending) {
            // can phai huy phuong thuc thanh toan truoc
            throw new BillException.NeedCancelFirst();
        }

        if (rechargeEntityDB.getBillStatus() != PaymentUtils.BILL_STATUS.Initial) {
            // neu khac trang thai init thi k cho update
            throw new BillException.NotFound();
        }
        if (rechargeReqDTO.getPromoCode() != null && !rechargeReqDTO.getPromoCode().trim().isEmpty()) {
            // apply promote code if have
            promoService.applyPromoCode(token, rechargeReqDTO.getPromoCode(), transactionId);
        }

        // check permission
        if (!rechargeEntityDB.getUserId().equals(userId)) {
            throw new AuthenticationException.UserDoesNotHaveAccess();
        }

        if (rechargeReqDTO.getPayMethod() != null) {
            rechargeEntityDB.setPaymentMethod(rechargeReqDTO.getPayMethod());
        }

        // step 3: cap nhat don hang
        PriceEntityDB priceEntityDB = getPrice(rechargeReqDTO.getPriceId());
        Long newPrice = getNewPrice(priceEntityDB, rechargeReqDTO.getPromoCode());
        rechargeEntityDB.setAmount(newPrice);
        rechargeEntityDB.setEmail((rechargeReqDTO.getEmail() == null
                || rechargeReqDTO.getEmail().isEmpty()) ? email : rechargeReqDTO.getEmail());
        rechargeEntityDB.setPriceId(rechargeReqDTO.getPriceId());
        rechargeEntityDB.setUpdatedDate(new Date());
        rechargeEntityDB.setDescription(Constants.PAY_DESCRIPTION.replace("[[transactionId]]", priceEntityDB.getName()));

        rechargeEntityDB = rechargeEntityRepo.save(rechargeEntityDB);

        RechargeResDTO rechargeResDTO = RechargeResDTO.from(rechargeEntityDB);
        rechargeResDTO.setItemId(priceEntityDB.getId());
        rechargeResDTO.setItemName(priceEntityDB.getName());
        rechargeResDTO.setItemAmount(priceEntityDB.getAmount());
        rechargeResDTO.setItemPrice(priceEntityDB.getPrice());
        rechargeResDTO.setPriceName(priceEntityDB.getName());
        rechargeResDTO.setPromoCode(rechargeReqDTO.getPromoCode());

        return rechargeResDTO;
    }

    /**
     * get detail of bill
     * @param token
     * @param transactionId
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws PriceException.NotFound
     * @throws BillException.NotFound
     */
    @Override
    @AuthenticationAOP()
    public RechargeResDTO getDetailBill(String token, String transactionId) throws
            AuthenticationException.MissingToken, PriceException.NotFound, BillException.NotFound {
        // step 1: check payment has existed or not
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);
        if (!rechargeEntityDBOptional.isPresent()) {
            throw new BillException.NotFound();
        }
        RechargeEntityDB rechargeEntityDB = rechargeEntityDBOptional.get();
        Long userId = authService.getUserId(token);

        if (!rechargeEntityDB.getUserId().equals(userId)) {
            throw new PriceException.NotFound();
        }
        PriceEntityDB priceEntityDB = getPrice(rechargeEntityDB.getPriceId());
        Optional<PromoHistoryEntityDB> promoHistoryEntityDBOptional =
                promoHistoryEntityRepo.findByTransactionId(transactionId);

        String promoCode = null;
        if (promoHistoryEntityDBOptional.isPresent()) {
            PromoHistoryEntityDB promoHistoryEntityDB = promoHistoryEntityDBOptional.get();
            Optional<PromoEntityDB> promoEntityDB =
                    promoEntityRepo.findById(promoHistoryEntityDB.getCampaignId());
            if (promoEntityDB.isPresent()) {
                promoCode = promoEntityDB.get().getPromoCode();
            }
        }

        RechargeResDTO rechargeResDTO = RechargeResDTO.from(rechargeEntityDB);
        rechargeResDTO.setPriceName(priceEntityDB.getName());
        rechargeResDTO.setItemId(priceEntityDB.getId());
        rechargeResDTO.setItemName(priceEntityDB.getName());
        rechargeResDTO.setItemAmount(priceEntityDB.getAmount());
        rechargeResDTO.setItemPrice(priceEntityDB.getPrice());
        rechargeResDTO.setPromoCode(promoCode);

        return rechargeResDTO;
    }

    /**
     * search my bill
     * @param token
     * @param pageable
     * @return
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public PagingResponseDTO<RechargeResDTO> searchBill(String token, Pageable pageable)
            throws AuthenticationException.MissingToken {
        Long userId = authService.getUserId(token);
        Page<RechargeEntityDB> rechargeEntityDBPage = rechargeEntityRepo.findAllByUserId(userId, pageable);
        List<RechargeEntityDB> rechargeEntityDBS = rechargeEntityDBPage.getContent();
        List<RechargeResDTO> rechargeResDTOS = new ArrayList<>();
        for (int i = 0; i < rechargeEntityDBS.size(); i++) {
            RechargeEntityDB rechargeEntityDB = rechargeEntityDBS.get(i);
            RechargeResDTO rechargeResDTO = RechargeResDTO.from(rechargeEntityDB);
            Long priceId = rechargeEntityDB.getPriceId();
            Optional<PriceEntityDB> entityDBOptional = priceEntityRepo.findById(priceId);
            if (entityDBOptional.isPresent()) {
                PriceEntityDB priceEntityDB = entityDBOptional.get();
                rechargeResDTO.setPriceName(priceEntityDB.getName());
                rechargeResDTO.setItemId(priceEntityDB.getId());
                rechargeResDTO.setItemName(priceEntityDB.getName());
                rechargeResDTO.setItemAmount(priceEntityDB.getAmount());
                rechargeResDTO.setItemPrice(priceEntityDB.getPrice());
            }
            rechargeResDTOS.add(rechargeResDTO);
        }
        return PagingResponseDTO.from(rechargeResDTOS, rechargeEntityDBPage.getTotalPages(), rechargeEntityDBPage.getTotalElements());
    }

    /**
     * cancel 1 bill
     * @param token
     * @param transactionId
     * @throws AuthenticationException.MissingToken
     * @throws BillException.NotFound
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public void cancelBill(String token, String transactionId) throws
            AuthenticationException.MissingToken, BillException.NotFound {
        // only update for initial status
        Long userId = authService.getUserId(token);
        // step 1: check payment has existed or not
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);
        if (!rechargeEntityDBOptional.isPresent()) {
            throw new BillException.NotFound();
        }

        // step 2: if existed -> need check can update
        RechargeEntityDB rechargeEntityDB = rechargeEntityDBOptional.get();

        if (rechargeEntityDB.getBillStatus() != PaymentUtils.BILL_STATUS.Pending
                && rechargeEntityDB.getBillStatus() != PaymentUtils.BILL_STATUS.Initial) {
            throw new BillException.NotFound();
        }

        // check permission
        if (!rechargeEntityDB.getUserId().equals(userId)) {
            throw new AuthenticationException.UserDoesNotHaveAccess();
        }

        // remove used promote code if using
        promoHistoryEntityRepo.deleteByTransactionId(transactionId);

        rechargeEntityDB.setBillStatus(PaymentUtils.BILL_STATUS.Cancel);
        rechargeEntityDB.setUpdatedDate(new Date());

        rechargeEntityRepo.save(rechargeEntityDB);
    }

    /**
     * perform pay bill
     * @param token
     * @param transactionId
     * @param payReqDTO
     * @throws BillException.NotFound
     * @throws BillException.CodeInvalid
     * @throws PriceException.NotFound
     */
    @Override
    @AuthenticationAOP()
    public void payBillCode(String token, String transactionId, PayReqDTO payReqDTO)
            throws BillException.NotFound, BillException.CodeInvalid, PriceException.NotFound, AuthenticationException.MissingToken {

        RechargeEntityDB rechargeEntityDB = validateBill(token, transactionId);

        if ((payReqDTO.getPayCode() == null || payReqDTO.getPayCode().isEmpty())
                && !rechargeEntityDB.getAmount().equals(0L)) {
            throw new BillException.CodeInvalid();
        }

        if (rechargeEntityDB.getAmount().equals(0L)) {
            perFormPay(rechargeEntityDB);
            return;
        }

        // check code is available
        Optional<PayCodeEntityDB> entityDBOptional =
                payCodeEntityRepo.findByPayCodeAndUsedIs(payReqDTO.getPayCode(), false);

        // if code not existed or code for another => invalid code
        if (!entityDBOptional.isPresent() ||
                !entityDBOptional.get().getTransactionId().equalsIgnoreCase(transactionId)) {
            throw new BillException.CodeInvalid();
        }

        // else valid => perform pay
        PayCodeEntityDB payCodeEntityDB = entityDBOptional.get();
        payCodeEntityDB.setUsed(true);
        payCodeEntityRepo.save(payCodeEntityDB);

        try {
            perFormPay(rechargeEntityDB);
        } catch (Exception e) {
            writeErrorHistory(rechargeEntityDB);
            throw e;
        }
    }

    /**
     * generate vnpay url
     * @param token
     * @param transactionId
     * @param request
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws BillException.NotFound
     */
    @Override
    @AuthenticationAOP()
    public String payBillVnpay(String token, String transactionId, HttpServletRequest request)
            throws AuthenticationException.MissingToken, BillException.NotFound {
        RechargeEntityDB rechargeEntityDB = validateBill(token, transactionId);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat(VnpayUtils.DATE_FORMAT_VNPAY);
        String vnp_CreateDate = formatter.format(calendar.getTime());
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());

        HashMap<String, String> params = new HashMap<>();
        params.put(VnpayUtils.VNPAY_VERSION_KEY, VnpayUtils.VNPAY_VERSION);
        params.put(VnpayUtils.VNPAY_COMMAND_KEY, VnpayUtils.VNPAY_PAY);
        params.put(VnpayUtils.VNPAY_TMN_KEY, VNPAY_CLIENT_ID);
        params.put(VnpayUtils.VNPAY_AMOUNT_KEY, String.valueOf(rechargeEntityDB.getAmount() * 100));
        params.put(VnpayUtils.VNPAY_CURRENT_CODE_KEY, VnpayUtils.VNPAY_CURRENT_CODE);
        params.put(VnpayUtils.VNPAY_TXN_KEY, transactionId);


        params.put(VnpayUtils.VNPAY_ORDER_INFO_KEY, rechargeEntityDB.getDescription());
        params.put(VnpayUtils.VNPAY_ORDER_TYPE_KEY, VnpayUtils.VNPAY_ORDER_TYPE);
        params.put(VnpayUtils.VNPAY_LOCALE_KEY, VnpayUtils.VNPAY_LOCALE);
        params.put(VnpayUtils.VNPAY_RETURN_URL_KEY, VNPAY_RETURN_URL);
        params.put(VnpayUtils.VNPAY_IP_KEY, VnpayUtils.getIpAddress(request));
        params.put(VnpayUtils.VNPAY_CREATE_DATE_KEY, vnp_CreateDate);
        params.put(VnpayUtils.VNPAY_EXPIRED_KEY, vnp_ExpireDate);

        String query = VnpayUtils.queryGenerator(params);
        String hashed = VnpayUtils.hashedData(VNPAY_SECRET, params);
        query = query + "&" + VnpayUtils.VNPAY_HASHED_KEY + "=" + hashed;

        String url = VNPAY_URL + "/" + VNPAY_PAY_PATH + "?" + query;

        log.info(url);

        return VNPAY_URL + "/" + VNPAY_PAY_PATH + "?" + query;
    }

    /**
     * handler vpay response
     * @param params
     * @throws UnsupportedEncodingException
     */
    @Override
    public HandlerVnpayResDTO handlerVnPay(HashMap<String, String> params) throws UnsupportedEncodingException {
        HashMap<String, String> field = new HashMap<>();
        for (String originKey : params.keySet()) {
            String newKey = URLEncoder.encode(originKey, StandardCharsets.US_ASCII.toString());
            String newVal = URLEncoder.encode(params.get(originKey), StandardCharsets.US_ASCII.toString());
            if (newVal != null && newVal.length() > 0) {
                field.put(newKey, newVal);
            }
        }
        String vnp_SecureHash = params.get(VnpayUtils.VNPAY_HASHED_KEY);
        if (field.containsKey(VnpayUtils.VNPAY_HASHED_KEY)) {
            field.remove(VnpayUtils.VNPAY_HASHED_KEY);
        }

        if (field.containsKey(VnpayUtils.VNPAY_HASHED_TYPE_KEY)) {
            field.remove(VnpayUtils.VNPAY_HASHED_TYPE_KEY);
        }

        String signValue = VnpayUtils.hashedAllFields(VNPAY_SECRET, field);
        String transactionId = params.get(VnpayUtils.VNPAY_TXN_KEY);


        HandlerVnpayResDTO handlerVnpayResDTO = HandlerVnpayResDTO.builder()
                .transactionId(transactionId)
                .status(PaymentUtils.TRAN_STATUS.Failure)
                .build();

        // handler transaction
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);
        RechargeEntityDB rechargeEntityDB;

        if (rechargeEntityDBOptional.isPresent()) {
            rechargeEntityDB = rechargeEntityDBOptional.get();
            if (signValue.equals(vnp_SecureHash)) {
                if ("00".equals(params.get(VnpayUtils.VNPAY_TRANSACTION_STATUS_KEY))) {
                    if (rechargeEntityDBOptional.isPresent()) {
                        try {
                            perFormPay(rechargeEntityDB);
                            handlerVnpayResDTO.setStatus(PaymentUtils.TRAN_STATUS.Successful);
                        } catch (Exception e) {
                            log.error("Pay failure {}", e.getMessage());
                            handlerVnpayResDTO.setStatus(PaymentUtils.TRAN_STATUS.Failure);
                            writeErrorHistory(rechargeEntityDB);
                        }
                    }
                } else {
                    handlerVnpayResDTO.setStatus(PaymentUtils.TRAN_STATUS.Failure);
                    log.error("Pay failure with code {}", params.get(VnpayUtils.VNPAY_TRANSACTION_STATUS_KEY));
                    writeErrorHistory(rechargeEntityDB);
                }
            } else {
                handlerVnpayResDTO.setStatus(PaymentUtils.TRAN_STATUS.Failure);
                log.error("Pay failure with invalid signature");
                writeErrorHistory(rechargeEntityDB);
            }
        } else {
            handlerVnpayResDTO.setStatus(PaymentUtils.TRAN_STATUS.Failure);
        }

        return handlerVnpayResDTO;
    }


    @Override
    public void refund(String token, String transactionId, PaymentUtils.REFUND_METHOD refundMethod) {

    }

    /**
     * validate bill
     * @param token
     * @param transactionId
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws BillException.NotFound
     */
    private RechargeEntityDB validateBill(String token, String transactionId) throws
            AuthenticationException.MissingToken, BillException.NotFound {
        Long userId = authService.getUserId(token);
        // check bill is existed
        Optional<RechargeEntityDB> rechargeEntityDBOptional =
                rechargeEntityRepo.findByTransactionId(transactionId);

        // neu khong ton tai hoac khong co bill cho thanh toan -> throw exception
        if (!rechargeEntityDBOptional.isPresent() ||
                (rechargeEntityDBOptional.get().getBillStatus() != PaymentUtils.BILL_STATUS.Initial &&
                        rechargeEntityDBOptional.get().getBillStatus() != PaymentUtils.BILL_STATUS.Pending)) {
            throw new BillException.NotFound();
        }

        if (!userId.equals(rechargeEntityDBOptional.get().getUserId())) {
            throw new BillException.NotFound();
        }
        return rechargeEntityDBOptional.get();
    }

    /**
     * write error history
     * @param rechargeEntityDB
     */
    private void writeErrorHistory(RechargeEntityDB rechargeEntityDB) {
        rechargeEntityDB.setBillStatus(PaymentUtils.BILL_STATUS.Failure);
        rechargeEntityDB.setUpdatedDate(new Date());
        rechargeEntityRepo.save(rechargeEntityDB);


        TransactionHistoryEntityDB transactionHistoryEntityDB = TransactionHistoryEntityDB.builder()
                .userId(rechargeEntityDB.getUserId())
                .method(PaymentUtils.TRAN_METHOD.Recharge)
                .transactionDate(new Date())
                .transactionStatus(PaymentUtils.TRAN_STATUS.Failure)
                .email(rechargeEntityDB.getEmail())
                .detail(Constants.RECHARGE_COMMON_FAILURE.replace("[[email]]", rechargeEntityDB.getEmail()))
                .build();
        transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
    }

    /**
     * pay bill
     * @param rechargeEntityDB
     * @throws PriceException.NotFound
     */
    @Transactional(rollbackOn = {Exception.class, Throwable.class})
    void perFormPay(RechargeEntityDB rechargeEntityDB) throws PriceException.NotFound {
        Long userId = rechargeEntityDB.getUserId();
        // check pay
        Optional<PriceEntityDB> entityDBOptional =
                priceEntityRepo.findById(rechargeEntityDB.getPriceId());
        if (!entityDBOptional.isPresent()) {
            throw new PriceException.NotFound();
        }
        PriceEntityDB priceEntityDB = entityDBOptional.get();

        // get current account
        Optional<WalletEntityDB> entityDBOptional1 =
                walletEntityRepo.findByUserId(userId);
        WalletEntityDB walletEntityDB = entityDBOptional1.get();

        // get type of course
        if (walletEntityDB.getWalletType() == PaymentUtils.WALLET_TYPE.PREMIUM) {
            if (priceEntityDB.getAccountType() == PaymentUtils.WALLET_TYPE.PREMIUM) {
                Date d = walletEntityDB.getAvailableDate();
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                c.add(Calendar.DATE, priceEntityDB.getAmount().intValue());
                d = c.getTime();
                walletEntityDB.setAvailableDate(d);
            } else {
                BigDecimal newAmount = walletEntityDB.getAmount().add(new BigDecimal(priceEntityDB.getAmount()));
                walletEntityDB.setAmount(newAmount);
            }
        } else {
            if (priceEntityDB.getAccountType() == PaymentUtils.WALLET_TYPE.PREMIUM) {
                Date d = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                c.add(Calendar.DATE, priceEntityDB.getAmount().intValue());
                d = c.getTime();
                walletEntityDB.setAvailableDate(d);
                walletEntityDB.setWalletType(PaymentUtils.WALLET_TYPE.PREMIUM);
            } else {
                walletEntityDB.setAmount(new BigDecimal(priceEntityDB.getAmount()));
            }
        }

        walletEntityRepo.save(walletEntityDB);

        // cap nhat trang thai thanh toan
        rechargeEntityDB.setBillStatus(PaymentUtils.BILL_STATUS.Success);
        rechargeEntityDB.setUpdatedDate(new Date());
        rechargeEntityRepo.save(rechargeEntityDB);

        // cap nhat promote code hist
        Optional<PromoHistoryEntityDB> promoHistoryEntityDB =
                promoHistoryEntityRepo.findByTransactionId(rechargeEntityDB.getTransactionId());
        if (promoHistoryEntityDB.isPresent()) {
            PromoHistoryEntityDB historyEntityDB = promoHistoryEntityDB.get();
            historyEntityDB.setApply(true);
            historyEntityDB.setApplyDate(new Date());
            promoHistoryEntityRepo.save(historyEntityDB);
        }

        // store history
        TransactionHistoryEntityDB transactionHistoryEntityDB = TransactionHistoryEntityDB.builder()
                .userId(userId)
                .walletId(walletEntityDB.getId())
                .amount(new BigDecimal(priceEntityDB.getAmount()))
                .method(PaymentUtils.TRAN_METHOD.Recharge)
                .transactionDate(new Date())
                .transactionStatus(PaymentUtils.TRAN_STATUS.Successful)
                .email(rechargeEntityDB.getEmail())
                .detail(Constants.RECHARGE.replace("[[amount]]", priceEntityDB.getName())
                        .replace("[[status]]", PaymentUtils.TRAN_STATUS.Successful.toString()))
                .build();
        transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
    }

    /**
     * get price data
     * @param priceId
     * @return
     * @throws PriceException.NotFound
     */
    private PriceEntityDB getPrice(Long priceId) throws PriceException.NotFound {
        // step 1: get detail of bought item
        Optional<PriceEntityDB> entityDBOptional = priceEntityRepo.findById(priceId);
        if (!entityDBOptional.isPresent()) {
            throw new PriceException.NotFound();
        }
        return entityDBOptional.get();
    }

    /**
     * get new price and return client
     * @param priceEntityDB
     * @param promoCode
     * @return
     */
    private Long getNewPrice(PriceEntityDB priceEntityDB, String promoCode) {
        Long discount = 0L;
        if (promoCode != null && !promoCode.isEmpty()) {
            Optional<PromoEntityDB> optionalPromoEntityDB =
                    promoEntityRepo.findByPromoCode(promoCode);
            if (optionalPromoEntityDB.isPresent()) {
                PromoEntityDB promoEntityDB = optionalPromoEntityDB.get();
                if (promoEntityDB.getPromoType() == PaymentUtils.PROMO_TYPE.Claim) {
                    discount = promoEntityDB.getAmount();
                } else {
                    discount = promoEntityDB.getAmount() * priceEntityDB.getPrice() / 100;
                }
            }
        }

        Long newPrice = priceEntityDB.getPrice() - discount;
        return newPrice;
    }
}
