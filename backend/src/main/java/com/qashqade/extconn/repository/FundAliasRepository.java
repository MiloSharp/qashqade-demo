package com.qashqade.extconn.repository;

import com.qashqade.extconn.model.FundAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundAliasRepository extends JpaRepository<FundAlias, Long> {
    List<FundAlias> findByFundIdOrderByAliasNameAsc(Long fundId);
    Optional<FundAlias> findByFundIdAndAliasNameIgnoreCase(Long fundId, String aliasName);
}
