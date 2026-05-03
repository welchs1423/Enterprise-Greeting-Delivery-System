package com.egds.blockchain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Web3j client configuration for Ethereum node connectivity.
 *
 * <p>In local development the endpoint defaults to a Ganache in-process
 * instance at {@code http://localhost:8545}. Production deployments
 * override {@code web3.ethereum.endpoint} to point at an Infura or
 * private geth/besu node.
 *
 * <p>The {@link Web3j} bean is shared across all components in the
 * {@code com.egds.blockchain} package. The underlying HTTP connection
 * pool is managed by OkHttp and is safe for concurrent use.
 */
@Configuration
public class Web3Config {

    /** Ethereum JSON-RPC endpoint resolved from application properties. */
    @Value("${web3.ethereum.endpoint:http://localhost:8545}")
    private String ethereumEndpoint;

    /**
     * Provides a singleton {@link Web3j} client connected to the
     * configured Ethereum endpoint.
     *
     * @return the {@link Web3j} client instance
     */
    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(ethereumEndpoint));
    }
}
