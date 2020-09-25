package com.template.ehelp;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilitiesKt;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemFungibleTokens;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilitiesKt;
import com.template.usd.UsdTokenConstants;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.util.Collections;

class RedeemEhelpFlow extends FlowLogic<SignedTransaction> {
    private final long amount;

    RedeemEhelpFlow(final long amount) {
        this.amount = amount;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final TokenType ehelpTokenType = FiatCurrency.Companion.getInstance("EHELP");
        final Party ehelpMint = getServiceHub().getNetworkMapCache().getPeerByLegalName(EhelpTokenConstants.EHELP_MINT);
        if (ehelpMint == null) throw new FlowException("No EHELP Mint found");

        // Describe how to find those $ held by Me.
        final QueryCriteria heldByMe = QueryUtilitiesKt.heldTokenAmountCriteria(ehelpTokenType, getOurIdentity());
        final Amount<TokenType> tokenAmount = AmountUtilitiesKt.amount(amount, ehelpTokenType);

        // Do the redeem
        return subFlow(new RedeemFungibleTokens(
                tokenAmount, // How much to redeem
                ehelpMint, // issuer
                Collections.emptyList(), // Observers
                heldByMe, // Criteria to find the inputs
                getOurIdentity())); // change holder
    }
}
