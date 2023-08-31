package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.services.interfaces.IPromoService;
import com.cloudnut.payment.infrastructure.entity.PromoEntityDB;
import com.cloudnut.payment.infrastructure.entity.WalletEntityDB;
import com.cloudnut.payment.infrastructure.repository.PromoEntityRepo;
import com.cloudnut.payment.infrastructure.repository.PromoHistoryEntityRepo;
import com.cloudnut.payment.infrastructure.repository.WalletEntityRepo;
import com.cloudnut.payment.utils.PaymentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    PromoEntityRepo promoEntityRepo;

    @Autowired
    PromoHistoryEntityRepo historyEntityRepo;

    @Autowired
    IPromoService promoService;

    @Autowired
    WalletEntityRepo walletEntityRepo;

    /**
     * everyday check account is out date. every 5 minutes
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 60000)
    @Transactional
    public void disablePremium() {
        List<WalletEntityDB> walletEntityDBS =
                walletEntityRepo.findAllByWalletType(PaymentUtils.WALLET_TYPE.PREMIUM);
        Date now = new Date();
        for (int i = 0; i < walletEntityDBS.size(); i++) {
            WalletEntityDB walletEntityDB = walletEntityDBS.get(i);
            if (now.after(walletEntityDB.getAvailableDate())) {
                walletEntityDB.setWalletType(PaymentUtils.WALLET_TYPE.PAYG);
                walletEntityRepo.save(walletEntityDB);
            }
        }
    }

    /**
     * check promocode available
     */
    @Scheduled(cron = "0 0/5 0 * * *")
    @Transactional
    public void EnablePromoCode() {
        try {
            // get list promo is not expire
            List<PromoEntityDB> promoEntityDBS = promoEntityRepo.findAllByExpiredDateAfter(new Date());
            for (int i = 0; i < promoEntityDBS.size(); i++) {
                PromoEntityDB promoEntityDB = promoEntityDBS.get(i);
                // check disable
                long usage = historyEntityRepo.countByCampaignId(promoEntityDB.getId());
                if (usage >= promoEntityDB.getTotalCode()) {
                    promoEntityDB.setActive(false);
                    promoEntityRepo.save(promoEntityDB);
                    continue;
                }

                if (usage < promoEntityDB.getTotalCode()
                        && promoEntityDB.getActive().equals(false)
                        && promoEntityDB.getAvailableDate().after(new Date())) {
                    promoEntityDB.setActive(true);
                    promoEntityRepo.save(promoEntityDB);
                }
            }

            // get all promo after now but still not deactive
            List<PromoEntityDB> promoEntityDBS1 = promoEntityRepo.findAllByExpiredDateBeforeAndActiveIs(new Date(), true);
            for (int i = 0; i < promoEntityDBS1.size(); i ++) {
                PromoEntityDB promoEntityDB = promoEntityDBS1.get(i);
                promoEntityDB.setActive(false);
                promoEntityRepo.save(promoEntityDB);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
