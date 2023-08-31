package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.request.price.PriceReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.price.PriceResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.application.services.interfaces.IPriceService;
import com.cloudnut.payment.infrastructure.entity.PriceEntityDB;
import com.cloudnut.payment.infrastructure.repository.PriceEntityRepo;
import com.cloudnut.payment.utils.PaymentUtils;
import com.cloudnut.payment.utils.RoleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PriceService implements IPriceService {
    @Autowired
    PriceEntityRepo priceEntityRepo;

    @Autowired
    IAuthService authService;

    /**
     * new new price
     * @param token
     * @param priceReqDTO
     * @return
     * @throws PriceException.AlreadyExisted
     * @throws AuthenticationException.MissingToken
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PriceResDTO createPrice(String token, PriceReqDTO priceReqDTO) throws PriceException.AlreadyExisted,
            AuthenticationException.MissingToken {
        String username = authService.getUserName(token);

        // find existed
        Optional<PriceEntityDB> entityDBOptional = priceEntityRepo.findByName(priceReqDTO.getName());

        if (entityDBOptional.isPresent()) {
            throw new PriceException.AlreadyExisted();
        }

        PriceEntityDB priceEntityDB = PriceEntityDB.builder()
                .name(priceReqDTO.getName())
                .accountType(PaymentUtils.WALLET_TYPE.getByName(priceReqDTO.getAccountType()))
                .amount(priceReqDTO.getAmount())
                .createdDate(new Date())
                .description(priceReqDTO.getDescription())
                .createdBy(username)
                .updatedBy(username)
                .updatedDate(new Date())
                .price(priceReqDTO.getPrice())
                .build();

        priceEntityDB = priceEntityRepo.save(priceEntityDB);

        return PriceResDTO.from(priceEntityDB);
    }

    /**
     * update price
     * @param token
     * @param id
     * @param priceReqDTO
     * @return
     * @throws AuthenticationException.MissingToken
     * @throws PriceException.NotFound
     * @throws PriceException.AlreadyExisted
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public PriceResDTO updatePrice(String token, Long id, PriceReqDTO priceReqDTO)
            throws AuthenticationException.MissingToken, PriceException.NotFound, PriceException.AlreadyExisted {
        String username = authService.getUserName(token);

        Optional<PriceEntityDB> entityDBOptional = priceEntityRepo.findById(id);
        if (!entityDBOptional.isPresent()) {
            throw new PriceException.NotFound();
        }

        PriceEntityDB priceEntityDB = entityDBOptional.get();

        // if update name => check name is existed or not
        if (!priceEntityDB.getName().equals(priceReqDTO.getName())) {
            Optional<PriceEntityDB> entityDBOptional1 = priceEntityRepo.findByName(priceReqDTO.getName());
            if (entityDBOptional1.isPresent()) {
                throw new PriceException.AlreadyExisted();
            }
        }

        priceEntityDB.setName(priceReqDTO.getName());
        priceEntityDB.setAccountType(PaymentUtils.WALLET_TYPE.getByName(priceReqDTO.getAccountType()));
        priceEntityDB.setPrice(priceReqDTO.getPrice());
        priceEntityDB.setAmount(priceReqDTO.getAmount());
        priceEntityDB.setDescription(priceReqDTO.getDescription());
        priceEntityDB.setUpdatedBy(username);
        priceEntityDB.setUpdatedDate(new Date());

        priceEntityRepo.save(priceEntityDB);

        return PriceResDTO.from(priceEntityDB);
    }

    /**
     * delete price
     * @param token
     * @param id
     */
    @Override
    @AuthenticationAOP(roles = RoleConstants.ADMIN)
    @Transactional
    public void deletePrice(String token, Long id) {
        priceEntityRepo.deleteById(id);
    }

    /**
     * search all price
     * @param token
     * @param searchText
     * @param pageable
     * @return
     */
    @Override
    @AuthenticationAOP()
    public PagingResponseDTO<PriceResDTO> searchPrice(String token, String searchText, Pageable pageable) {
        Page<PriceEntityDB> priceEntityDBPage =
                priceEntityRepo.findAllByNameContainingIgnoreCase(searchText, pageable);
        List<PriceEntityDB> priceEntityDBS = priceEntityDBPage.getContent();
        List<PriceResDTO> priceResDTOS =
                priceEntityDBS.stream().map(PriceResDTO::from).collect(Collectors.toList());
        return PagingResponseDTO.from(priceResDTOS, priceEntityDBPage.getTotalPages(),
                priceEntityDBPage.getTotalElements());
    }

    /**
     * get all price
     * @param token
     * @return
     */
    @Override
    // @AuthenticationAOP()
    public List<PriceResDTO> getAllPrice() {
        List<PriceEntityDB> priceEntityDBS = priceEntityRepo.findAll();
        return priceEntityDBS.stream().map(PriceResDTO::from).collect(Collectors.toList());
    }
}
