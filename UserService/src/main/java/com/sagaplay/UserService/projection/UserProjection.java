package com.sagaplay.UserService.projection;

import com.sagaplay.CommonService.model.CardDetails;
import com.sagaplay.CommonService.model.User;
import com.sagaplay.CommonService.queries.GetUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

    @QueryHandler
    public User getUserPaymentDetails(GetUserPaymentDetailsQuery query) {
        // ideally this details should come from DB
        CardDetails cardDetails = CardDetails.builder()
                .name("Michael Su")
                .validUntilyear(2022)
                .validUntilMonth(3)
                .cardNumber("12314564879")
                .cvv(234)
                .build();

        return User.builder()
                .userId(query.getUserId())
                .firstName("Michael")
                .lastName("Su")
                .cardDetails(cardDetails)
                .build();
    }
}
