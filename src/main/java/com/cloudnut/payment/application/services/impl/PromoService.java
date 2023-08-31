package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.request.promo.PromoCodeReqDTO;
import com.cloudnut.payment.application.dto.request.promo.PromoUpdateReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoResDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoSumResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.exception.PromoException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.application.services.interfaces.IPromoService;
import com.cloudnut.payment.infrastructure.entity.PromoEntityDB;
import com.cloudnut.payment.infrastructure.entity.PromoHistoryEntityDB;
import com.cloudnut.payment.infrastructure.repository.PromoEntityRepo;
import com.cloudnut.payment.infrastructure.repository.PromoHistoryEntityRepo;
import com.cloudnut.payment.utils.PaymentUtils;
import com.cloudnut.payment.utils.RoleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PromoService implements IPromoService {

    @Autowired
    IAuthService authService;

    @Autowired
    PromoEntityRepo promoEntityRepo;

    @Autowired
    PromoHistoryEntityRepo promoHistoryEntityRepo;

    /**
     * create new promote code
     * @param token
     * @param promoCodeReqDTO
     * @return
     * @throws PromoException.AlreadyExisted
     * @throws AuthenticationException.MissingToken
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PromoResDTO createPromo(String token, PromoCodeReqDTO promoCodeReqDTO)
            throws PromoException.AlreadyExisted, AuthenticationException.MissingToken {
        checkPromoExisted(promoCodeReqDTO.getPromoCode());
        PromoEntityDB entityDB = PromoEntityDB.builder()
                .name(promoCodeReqDTO.getName())
                .description(promoCodeReqDTO.getDescription())
                .promoCode(promoCodeReqDTO.getPromoCode())
                .availableDate(PaymentUtils.formatUTC(promoCodeReqDTO.getAvailableDate()))
                .expiredDate(PaymentUtils.formatUTC(promoCodeReqDTO.getExpiredDate()))
                .totalCode(promoCodeReqDTO.getTotalCode())
                .promoType(promoCodeReqDTO.getPromoType())
                .amount(promoCodeReqDTO.getAmount())
                .active(false)
                .build();
        entityDB = promoEntityRepo.save(entityDB);
        return PromoResDTO.from(entityDB, 0L);
    }

    /**
     * update promo code
     * @param token
     * @param id
     * @param promoUpdateReqDTO
     * @return
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PromoResDTO updatePromo(String token, Long id, PromoUpdateReqDTO promoUpdateReqDTO)
            throws PromoException.NotFound {
        Optional<PromoEntityDB> entityDBOptional = promoEntityRepo.findById(id);
        if (!entityDBOptional.isPresent()) {
            throw new PromoException.NotFound();
        }

        PromoEntityDB promoEntityDB = entityDBOptional.get();

        promoEntityDB.setDescription(promoUpdateReqDTO.getDescription());
        promoEntityDB.setAvailableDate(PaymentUtils.formatUTC(promoUpdateReqDTO.getAvailableDate()));
        promoEntityDB.setExpiredDate(PaymentUtils.formatUTC(promoUpdateReqDTO.getExpiredDate()));
        promoEntityDB.setTotalCode(promoUpdateReqDTO.getTotalCode());

        promoEntityDB = promoEntityRepo.save(promoEntityDB);

        long usedCount = promoHistoryEntityRepo.countByCampaignId(id);

        return PromoResDTO.from(promoEntityDB, usedCount);
    }

    /**
     * delete promo
     * @param token
     * @param id
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public void deletePromo(String token, Long id) {
        promoEntityRepo.deleteById(id);
        promoHistoryEntityRepo.deleteByCampaignId(id);
    }

    /**
     * inactive promote code
     * @param token
     * @param id
     * @param action
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public void inactivePromo(String token, Long id, PaymentUtils.ACTION action) throws PromoException.NotFound {
        inactivePromo(id, action);
    }

    /**
     * inactive promote code
     * @param id
     * @param action
     */
    @Override
    @Transactional
    public void inactivePromo(Long id, PaymentUtils.ACTION action) throws PromoException.NotFound {
        Optional<PromoEntityDB> entityDBOptional = promoEntityRepo.findById(id);
        if (!entityDBOptional.isPresent()) {
            throw new PromoException.NotFound();
        }

        PromoEntityDB promoEntityDB = entityDBOptional.get();
        if (action == PaymentUtils.ACTION.active) {
            promoEntityDB.setActive(true);
        } else {
            promoEntityDB.setActive(false);
        }
        promoEntityRepo.save(promoEntityDB);
    }


    /**
     * search promote code
     * @param token
     * @param searchText
     * @param pageable
     * @return
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    public PagingResponseDTO<PromoResDTO> searchPromo(String token, String searchText, Pageable pageable) {
        Page<PromoEntityDB> promoEntityDBPage =
                promoEntityRepo.findAllByNameContainingIgnoreCaseOrPromoCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchText, searchText, searchText, pageable);
        List<PromoEntityDB> promoEntityDBS = promoEntityDBPage.getContent();
        List<PromoResDTO> promoResDTOS = new ArrayList<>();
        for (int i = 0; i < promoEntityDBS.size(); i++) {
            long used = promoHistoryEntityRepo.countByCampaignId(promoEntityDBS.get(i).getId());
            PromoResDTO promoResDTO = PromoResDTO.from(promoEntityDBS.get(i), used);
            promoResDTOS.add(promoResDTO);
        }
        return PagingResponseDTO.from(promoResDTOS, promoEntityDBPage.getTotalPages(), promoEntityDBPage.getTotalElements());
    }

    /**
     * get all promo code for user/search
     * @return
     */
    @Override
    // @AuthenticationAOP()
    public List<PromoSumResDTO> getAllPromo() {
        List<PromoSumResDTO> promoResDTOS = new ArrayList<>();
        List<PromoEntityDB> promoEntityDBS = promoEntityRepo.findAllByActiveIs(true);
        for (int i = 0; i < promoEntityDBS.size(); i++) {
            PromoSumResDTO promoResDTO = PromoSumResDTO.from(promoEntityDBS.get(i));
            promoResDTOS.add(promoResDTO);
        }
        return promoResDTOS;
    }

    /**
     * apply promo code
     * @param token
     * @param promoCode
     * @return
     * @throws PromoException.NotAvailable
     * @throws PromoException.OutOfTotal
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public PromoSumResDTO applyPromoCode(String token, String promoCode, String transactionId)
            throws PromoException.NotAvailable, PromoException.OutOfTotal,
            AuthenticationException.MissingToken {
        // xóa trắng dữ liệu nếu chưa apply
        promoHistoryEntityRepo.deleteByTransactionIdAndApplyIs(transactionId, false);
        PromoEntityDB promoEntityDB = applyCode(promoCode, transactionId);
        return PromoSumResDTO.from(promoEntityDB);
    }

    /**
     * apply promote code and not return anything
     * @param token
     * @param promoCode
     * @throws AuthenticationException.MissingToken
     * @throws PromoException.OutOfTotal
     * @throws PromoException.NotAvailable
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public void applyPromoCodeVoid(String token, String promoCode, String transactionId) throws
            AuthenticationException.MissingToken, PromoException.OutOfTotal, PromoException.NotAvailable {
        // xóa trắng dữ liệu nếu chưa apply
        promoHistoryEntityRepo.deleteByTransactionIdAndApplyIs(transactionId, false);
        if (promoCode != null && !promoCode.isEmpty()) {
            applyCode(promoCode, transactionId);
        }
    }

    /**
     * apply by code
     * @param promoCode
     * @param transactionId
     * @return
     * @throws PromoException.NotAvailable
     * @throws PromoException.OutOfTotal
     */
    private PromoEntityDB applyCode(String promoCode, String transactionId)
            throws PromoException.NotAvailable, PromoException.OutOfTotal {
        Optional<PromoEntityDB> entityDBOptional = promoEntityRepo.findByPromoCode(promoCode);
        if (!entityDBOptional.isPresent()) {
            throw new PromoException.NotAvailable();
        }
        PromoEntityDB promoEntityDB = entityDBOptional.get();
        long sum = promoHistoryEntityRepo.countByCampaignId(promoEntityDB.getId());
        if (!promoEntityDB.getActive() || promoEntityDB.getExpiredDate().before(new Date())) {
            throw new PromoException.NotAvailable();
        }

        if (sum > promoEntityDB.getTotalCode()) {
            throw new PromoException.OutOfTotal();
        }

        // delete all promo still not apply
        promoHistoryEntityRepo.deleteByTransactionIdAndApplyIs(transactionId, false);

        // create new apply
        PromoHistoryEntityDB promoHistoryEntityDB = PromoHistoryEntityDB.builder()
                .transactionId(transactionId)
                .campaignId(promoEntityDB.getId())
                .apply(false)
                .applyDate(new Date())
                .build();
        promoHistoryEntityRepo.save(promoHistoryEntityDB);
        return promoEntityDB;
    }

    /**
     * check existed by code
     * @param promoCode
     * @throws PromoException.AlreadyExisted
     */
    private void checkPromoExisted(String promoCode) throws PromoException.AlreadyExisted {
        Optional<PromoEntityDB> entityDB = promoEntityRepo.findByPromoCode(promoCode);
        if (entityDB.isPresent()) {
            throw new PromoException.AlreadyExisted();
        }
    }
}
