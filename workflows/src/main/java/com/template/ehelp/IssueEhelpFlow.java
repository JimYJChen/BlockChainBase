package com.template.ehelp;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import com.template.usd.UsdTokenConstants;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class IssueEhelpFlow extends FlowLogic<SignedTransaction> {

    @NotNull
    private final Party receiver;
    private final long amount;

    public IssueEhelpFlow(@NotNull final Party receiver, final long amount) {
        this.receiver = receiver;
        this.amount = amount;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final TokenType ehelpTokenType = new TokenType("EHELP", 2);
        if (!getOurIdentity().getName().equals(EhelpTokenConstants.EHELP_MINT)) {
            throw new FlowException("We are not the EHELP Mint");
        }
        final IssuedTokenType usMintUsd = new IssuedTokenType(getOurIdentity(), ehelpTokenType);

        // Who is going to own the output, and how much?
        // Create a 100$ token that can be split and merged.
        final Amount<IssuedTokenType> amountOfToken = AmountUtilitiesKt.amount(amount, usMintUsd);
        final FungibleToken ehelpToken = new FungibleToken(amountOfToken, receiver, null);

        // Issue the token to alice.
        return subFlow(new IssueTokens(
                Collections.singletonList(ehelpToken), // Output instances
                Collections.emptyList())); // Observers
    }
}
