package com.example.demo.infrastructure.msgraph;

import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MicrosoftGraphClient {

    @Value("${spring.security.oauth2.client.provider.azure.tenant-id}")
    private String tenantId;
    @Value("${spring.security.oauth2.client.registration.azure.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.azure.client-secret}")
    private String clientSecret;

    public GraphServiceClient getDelegateClient(String accessToken) {
        var credential = new OnBehalfOfCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .userAssertion(accessToken)
                .build();

        return new GraphServiceClient(credential);
    }

    public Message getMessage(String userId, String messageId, String accessToken) {
        GraphServiceClient graphClient = getDelegateClient(accessToken);

        return graphClient.users().byUserId(userId).messages().byMessageId(messageId)
                .get(requestConfiguration -> {
                    assert requestConfiguration.queryParameters != null;
                    requestConfiguration.queryParameters.select = new String[]{"subject", "body", "from"};
                });
    }

    public User getUserFromGraph(String accessToken) {
        GraphServiceClient graphClient = getDelegateClient(accessToken);
        return graphClient.me().get();
    }
}
