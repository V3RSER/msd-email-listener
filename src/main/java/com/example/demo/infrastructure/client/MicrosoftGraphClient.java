package com.example.demo.infrastructure.client;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.graph.models.ChangeType;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;

@Component
public class MicrosoftGraphClient {

    public GraphServiceClient getAuthenticatedClient(String accessToken) {
        TokenCredential credential = new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return Mono.just(new AccessToken(accessToken, OffsetDateTime.MAX));
            }
        };

        return new GraphServiceClient(credential);
    }

    public Mono<Message> getMessage(String userId, String messageId, String accessToken) {
        return Mono.fromCallable(() -> {
            GraphServiceClient graphClient = getAuthenticatedClient(accessToken);
            return graphClient.users().byUserId(userId).messages().byMessageId(messageId)
                    .get(requestConfiguration -> {
                        assert requestConfiguration.queryParameters != null;
                        requestConfiguration.queryParameters.select = new String[]{"subject", "body", "from"};
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> getUserFromGraph(String accessToken) {
        return Mono.fromCallable(() -> {
            GraphServiceClient graphClient = getAuthenticatedClient(accessToken);
            return graphClient.me().get();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Subscription> createSubscription(String userId, String notificationUrl, String accessToken) {
        return Mono.fromCallable(() -> {
            GraphServiceClient graphClient = getAuthenticatedClient(accessToken);

            Subscription subscription = new Subscription();
            subscription.setChangeType(ChangeType.Created.value);
            subscription.setNotificationUrl(notificationUrl);
            subscription.setResource("users/" + userId + "/messages");
            // Expiration is max 3 days for this resource. Let's set it to 1 hour for development.
            subscription.setExpirationDateTime(OffsetDateTime.now().plusHours(1));
            subscription.setClientState("SecretClientState"); // A secret state to validate notifications

            return graphClient.subscriptions().post(subscription);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
