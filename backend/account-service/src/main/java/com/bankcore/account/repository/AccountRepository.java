package com.bankcore.account.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankcore.account.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

}
