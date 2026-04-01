package com.example.demo.infrastructure.msgraph;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import com.microsoft.kiota.authentication.AzureIdentityAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MicrosoftGraphClient {

    @Value("${azure.tenant-id}")
    private String tenantId;
    @Value("${azure.client-id}")
    private String clientId;
    @Value("${azure.client-secret}")
    private String clientSecret;

    public GraphServiceClient<Request> getDelegateClient(String accessToken) {
        var credential = new OnBehalfOfCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .userAssertion(accessToken)
                .build();

        var authProvider = new AzureIdentityAuthenticationProvider(credential, new String[]{"https://graph.microsoft.com/.default"});

        return new GraphServiceClient<>(authProvider, null);
    }

    public Message getMessage(String userId, String messageId, String accessToken) {
        GraphServiceClient<Request> graphClient = getDelegateClient(accessToken);

        return graphClient.users(userId).messages(messageId)
                .buildRequest()
                .select("subject,body,from")
                .get();
    }

    public User getUserFromGraph(String accessToken) {
        GraphServiceClient<Request> graphClient = getDelegateClient(accessToken);
        return graphClient.me().buildRequest().get();
    }
}
